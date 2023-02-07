## Quick start
1. Just start
- Default Current Path

```
    > java -jar catlogging.war
```

2. Option Start (recommend)
```
    > nohup java -Dfile.encoding=UTF-8 -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Xms2048M -Xmx2096M -server -XX:+UseParallelGC -Dcatlogging.validationPath=/etc/ -Dcatlogging.home=/home/XXX -jar catlogging.war > /dev/null 2>&1 &
``` 

## Documentation

- Exec ENV : Linux,Windows,Mac..
- Add Support i18n(en, kr, ch)
- Add Support Log Validation path (For inaccessibility of basic system path.)
- Change Dark theme UI
![login-sample](image/Dashboard-Sample.png)
- Change SpringBoot 2.x App
- Enable Spring Secure
![login-sample](image/Login-Sample.png)
    - [properties] default web.servlet.session.timeout : 1800sec(30min) 
    - [properties] default catlogging.enable.auth : true
        - Whether or not to use the login function.
        Sometimes, in places where security is not necessary, you can set it to false for quick access. 
    - default username : admin
    - default password : admin  
- RealTime logging
![login-sample](image/logging-Sample.png)
   
- PATCH : secure log4j(CVE-2021-42550)

## Develop Run Info

- Enable external tomcat Run (default embed tomcat 9.x)
- Enable SpringBoot 2.x Run
- Enable H2 Remote access. (default : false)

### H2 Connection Info 
- username : catlogging
- password : catlogging 
- url : jdbc:h2:tcp://localhost:9095/`opt:catlogging.home`/h2/catlogging
> Example
> ```bash
> jdbc:h2:tcp://localhost:9095/home/catuser/h2/catlogging
> ```

### Directory Structure Info
```java
-
 |- config.properties
 |- elasticsearch-x.x.xx
 |- elasticsearch-x.x.xx.zip
 |- elasticsearch-x.x.xx.zip-downloaded
 |- h2
 |- logs

```