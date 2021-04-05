pipeline {
    agent {
      node {
        label "master"
      } 
    }

    stages {
      stage('fetch_latest_code') {
        steps {
          git credentialsId: 'esteban0477', url: 'https://github.com/esteban0477/cfn_aws'
        }
      }

      stage('Deploy cfn') {
        steps {
          if (env.BRANCH_NAME == 'main'){
            
            def MODULES_TO_BUILD = sh script:"./detect_tf_changes.sh", returnStdout: true
            MODULES_TO_BUILD.split("\n").each {
              
            stage('Create ChangeSet') {
                steps {
                  withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'awsjuan', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    sh 'aws cloudformation validate-template --template-body file://${MODULES_TO_BUILD}/cfn_stack_spec.yaml'
                    sh 'cat ${MODULES_TO_BUILD}/parameters.yaml | yq eval -PMj > ${MODULES_TO_BUILD}/parameters.json'
                    sh 'aws cloudformation create-stack --stack-name ${MODULES_TO_BUILD} --template-body file://${MODULES_TO_BUILD}/cfn_stack_spec.yaml --parameters file://${MODULES_TO_BUILD}/parameters.json'
                  }
                }      
              }
            }

          } else {
            
            def MODULES_TO_BUILD = sh script:"./detect_tf_changes.sh", returnStdout: true
            MODULES_TO_BUILD.split("\n").each {
              
            stage('Validate template') {
                steps {
                  withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'awsjuan', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    sh 'aws cloudformation validate-template --template-body file://${MODULES_TO_BUILD}/cfn_stack_spec.yaml'
                  }
                }      
              }
            }
          }
        }
      }
    }
}