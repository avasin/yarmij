# yarmij
Yet another RMI for Java

This project was created because I was deeply disappointed when I decided to write a pretty small and dumb client-server applicaion for Android.
Here is why:
1. Android by itseld does not support java.rmi package, so native implementation from Java cannot be used to solve the task described above.
2. Standard way to solve the goal from the top on Android is to use grpc library, but it brings the following unnecessary complexity:
    1. It requires at least one additional *.proto file with message and service declarations.
    2. It requires maven build script dedicated configurations, so *.java files will be automatically regenerated every build.
    3. Source code requires additional level of conversion process. In case you have your own model, you cannot use it directly as messages or message parts. You would need to convert your own model into *.java classes generated from *.proto message definitions. 
       
All 2.* subitems above are bringing significant inconvinience in case you need to write something simple and quick and you don't want to waste time on doing proper configurations.

There is only one significant restriction which is coming from serialization, which is provided by [ EsotericSoftware /
kryo ](https://github.com/EsotericSoftware/kryo) library - you need to define at least private no-arg constructor, so your class would be able to be instantiated.
