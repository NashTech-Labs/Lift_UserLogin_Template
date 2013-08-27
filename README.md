Lift_UserLogin_Template
=======================

This is LiftWeb Login Template with a nice User Interface.  If you want to use Liftweb as framework , Scala as Programming Language and 
MongoDB as Database then  this demo project can be used as a starting point for your application . In this 
project , we have also exposed services for REST api . 

*************************************************************************************************************
###Features:
*************************************************************************************************************
1.  Social Login Service :  This application allows you to login with one click by using social network account such as Facebook and Google .
1.  Email SignUP using MongoDB : If you don'y have social accounts , you can register yourself manually in application by entering a valid email id and password .
1.  Setting: Once logged in the application , you can change profile setting , account setting and password setting .
1.  Birthday Reminder Functionality : We have integrated Birthday Reminder functionality in this application . User can manage his/her friend's birthday  .
1.  REST Api :In this application , we have also exposed services for REST api .

*************************************************************************************************************
###Requirements : 
*************************************************************************************************************
1.  SBT
1.  MongoDB

*************************************************************************************************************
###Getting Started with Code  : 


*************************************************************************************************************
1.  Set Up MongoDB
1.  Start mongo: > mongod
1.  Import into Eclipse by 
    >sbt eclipse
1.  Start application by 
    >sbt ~container:start
1.  To Run test cases 
    >sbt test
    

*************************************************************************************************************
###User Journey  : 

*************************************************************************************************************

1.  User lands on login page for the first time .
1.  To create new account , user clicks on Sign Up .
1.  User enters user name , valid email address and password .
1.  After creating new account , user lands on login page again . 
1.  User enters his/her email address and password and hits login button .
1.  After logging in , user sees six menus on the main page .
1.  User will see Bday alert on home page , if his/her friend have Bday on that particular day.
1.  User sees his/her Gravatar image on the right side . 
1.  User clicks on "Profile" to see his/her profile detail .
1.  User clicks on "Edit your profile" on "Profile" page to edit his/her profile.
1.  User clicks on "Account Setting" to update account detail .
1.  User clicks on "Password Setting" to update password .
1.  User clicks on "Logout" to exist .
1.  User clicks on "Birthday Reminder" to manage Birthday Reminders .


*************************************************************************************************************
### Manage Birthday Reminder functionality  : 

*************************************************************************************************************
1.  After logging in , user sees "Birthday Reminder" menu on the main page . 
1.  User clicks on "Birthday Reminder" to add his/her friend's birthdate in reminder list .
1.  User enters friend's name , select birth date and hits Add button.
1.  Friend's name text box has Auto complete functionality. User can select from drop down list of auto complete as well as he can enter his   own value.
1.  As user will hit Add button, that birthdate will be added in below table.
1.  Table contains list of Birthday Reminders.
1.  User can delete and edit reminder by clicking on Delete and Edit button respectively from table.

*************************************************************************************************************
### How to test in remote environment:

*************************************************************************************************************
1.  Open a terminal and run sbt ~container:start.
1.  Open build.sbt and change
1.  port in container.Configuration := 8081
1.  Open another terminal and run sbt ~container:start.
1.  Register yourself and login and Enjoy chat.

You would be able to do chat remotely from different JVMs. Check and let me know your feedback. Your feedback would be highly appreciated.
