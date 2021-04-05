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
          sh 'chmod +x validate_existence.sh'
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
                  // def STACK_NAME_EXISTS = sh script:"../validate_existence.sh $STACK_NAME", returnStdout: true
                  /* if ("$STACK_NAME_EXISTS" == 0) {
                    sh 'echo "Stack does not exist"'
                    // sh 'aws cloudformation create-stack --template-body file://cfn_stack_spec.yaml --parameters file://parameters.json --stack-name '+STACK_NAME
                  } else {
                    sh 'echo "Stack exists"'
                    // sh 'aws cloudformation update-stack --template-body file://cfn_stack_spec.yaml --parameters file://parameters.json --stack-name '+STACK_NAME
                  }*/
                }
              }
            }
          }    
        }
      }
    }
}