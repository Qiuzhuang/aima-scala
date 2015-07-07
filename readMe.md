# How To Setup #

  * Install [http://maven.apache.org Maven ](.md)
  * Install Sun JDK
  * Install your favourite svn client.
  * Get the source code using svn from [http://aima-scala.googlecode.com/svn/trunk/](http://aima-scala.googlecode.com/svn/trunk/)
  * Go to base directory and run "$mvn clean test"; It should compile the code, run all the tests and prompt "Build Successful"
  * Happy Hacking

# Project Organization #

Directory organization is based on the standard Maven directory layout.
```
aima-scala
  src
    main
      scala
        aima
          basic          //Code for Chapter 2
          commons        //Common code used by all
          logic          //Code for Chapter 7-9
          planning       //Code for Chapter 10-11
          search         //Code for Chapter 3-6
          uncertainty    //Code for Chapter 13-
    test                 //Tests for above code
      scala
        aima
          basic
          logic
          planning
          search
          uncertainty
```
Best place to start to understand the code is to start with reading the tests for same.

# Bugs: #
Bug reports are well appreciated, When you send in a bug report, Please include following:

  * What you did to see the bug
  * What did you expect to see
  * What you actually saw
  * If possible, then a test written in scala that uncovers the bug