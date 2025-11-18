# ChangeIt

A commandline tool written in kotlin to make software development easier.

## Build
Use maven to build a .jar file.

``mvn clean install``

## Alias
To run the command we run the .jar file. To abbreviate the command we can set an alias.

``alias cit "java -cp '<absolute path>/target/changeit.jar' nl.sybr.dev.Application"``

## Run
Now you can run all defined commands.

```shell
cit add --source=.gitignore --target=src/main/resources/.gitignore
cit copy --files='*.yaml' --operation='test/%s'
cit delete --files='*.yaml
cit move --files='*.yaml --operation='test/%s'
cit update --files=CODEOWNERS --matcher='@sybren' --replacement'@sybr'
cit mr --gitlevel=mr
```

## Controls
With some other commands you get more control on these powerfull file commands.

```shell
cit history show
cit revert
cit redo
cit history delete -n 3
```
