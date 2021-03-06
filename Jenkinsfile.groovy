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
          sh 'chmod +x deploy_stack.sh'
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
                  sh "../deploy_stack.sh ${it}"
                }
              }
            }
          }    
        }
      }
    }
}