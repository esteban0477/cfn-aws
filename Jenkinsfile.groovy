pipeline {
    agent {
      node {
        label "master"
      } 
    }

    stages {
      stage('fetch_latest_code') {
        steps {
          git credentialsId: 'esteban0477', url: 'https://github.com/esteban0477/cfn-aws'
          sh 'chmod +x detect_tf_changes.sh'
          sh 'chmod +x get_stack_name.sh'
        }
      }

      stage('Deploy cfn') {
        steps {
          script {
            def MODULES_TO_BUILD = sh script:"./detect_tf_changes.sh", returnStdout: true
            MODULES_TO_BUILD.split("\n").each {
              withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'awsjuan', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                dir("${it}"){
                  sh 'pwd'
                  sh 'aws cloudformation validate-template --template-body file://cfn_stack_spec.yaml'
                  def STACK_NAME = sh script:"../get_stack_name.sh ${it}", returnStdout: true
                  def STACK_NAME_EXISTS = sh script:"aws cloudformation list-stacks --stack-status-filter REVIEW_IN_PROGRESS CREATE_FAILED UPDATE_ROLLBACK_FAILED UPDATE_ROLLBACK_IN_PROGRESS CREATE_IN_PROGRESS IMPORT_ROLLBACK_IN_PROGRESS UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS ROLLBACK_IN_PROGRESS IMPORT_IN_PROGRESS UPDATE_COMPLETE UPDATE_IN_PROGRESS DELETE_FAILED IMPORT_COMPLETE DELETE_IN_PROGRESS ROLLBACK_COMPLETE ROLLBACK_FAILED IMPORT_ROLLBACK_COMPLETE UPDATE_COMPLETE_CLEANUP_IN_PROGRESS CREATE_COMPLETE IMPORT_ROLLBACK_FAILED UPDATE_ROLLBACK_COMPLETE | jq .StackSummaries[].StackName -r | grep -c $STACK_NAME", returnStdout: true
                  if (STACK_NAME_EXISTS > 0) {
                    sh 'echo "Stack exists"'
                    // sh 'aws cloudformation update-stack --template-body file://cfn_stack_spec.yaml --parameters file://parameters.json --stack-name '+STACK_NAME
                  } else {
                    sh 'echo "Stack does not exist"'
                    // sh 'aws cloudformation create-stack --template-body file://cfn_stack_spec.yaml --parameters file://parameters.json --stack-name '+STACK_NAME
                  }
                  
                }
              }
            }
          }    
        }
      }
    }
}