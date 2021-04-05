#! /bin/bash
STACK_NAME=$(echo $1| tr -d '\n' | tr -d '/')
EXISTS=$(aws cloudformation list-stacks --stack-status-filter REVIEW_IN_PROGRESS CREATE_FAILED UPDATE_ROLLBACK_FAILED UPDATE_ROLLBACK_IN_PROGRESS CREATE_IN_PROGRESS IMPORT_ROLLBACK_IN_PROGRESS UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS ROLLBACK_IN_PROGRESS IMPORT_IN_PROGRESS UPDATE_COMPLETE UPDATE_IN_PROGRESS DELETE_FAILED IMPORT_COMPLETE DELETE_IN_PROGRESS ROLLBACK_COMPLETE ROLLBACK_FAILED IMPORT_ROLLBACK_COMPLETE UPDATE_COMPLETE_CLEANUP_IN_PROGRESS CREATE_COMPLETE IMPORT_ROLLBACK_FAILED UPDATE_ROLLBACK_COMPLETE | jq .StackSummaries[].StackName -r | grep -c $STACK_NAME)
echo $EXISTS
if [ $EXISTS -gt 0 ]
then
    aws cloudformation update-stack --template-body file://${STACK_NAME}/cfn_stack_spec.yaml --parameters file://${STACK_NAME}/parameters.json --stack-name ${STACK_NAME}
else
    aws cloudformation create-stack --template-body file://${STACK_NAME}/cfn_stack_spec.yaml --parameters file://${STACK_NAME}/parameters.json --stack-name ${STACK_NAME}
fi