# cfn-aws

Simple Repo to push config-files as stacks in order to deploy infrastructore to my personal account!

## Conventions: 
- The name of the stack will be the name of the folder where two files describe the desired state:
    1. cfn_stack_spec.yaml: This file is the core template which can use other nested stacks
    2. parameters.json: List of parameters expected by cfn_stack_spec.yaml

- If the files are not named as described in last convetion, the pipeline will fail

- The pipeline will validate if cfn_stack_spec.yaml is properly formatted using validate-template cli. If there are not default values declared for the parameters, the validation will fail

[Pending]

- Create Change sets

- Include pipeline for deleting stacks
