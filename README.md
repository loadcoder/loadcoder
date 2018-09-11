## 1.  Introduction

Loadcoder is a tool for javacoders to create and run load towards application/code to be performance tested.

Visit [loadcoder.com](http://loadcoder.com) for further information


## 2. Version Control
The sources for this library are kept in the following repository at GitHub:

    https://github.com/loadcoder/loadcoder

## 3. Community

If you would like to contribute to Loadcoder, introduce yourself through info@loadcoder.com and elaborate what you think of Loadcoder and your thoughts of taking the tool further.

The next step is that you will be invited to the loadcoder github organisation as a member.

### 3.1 Issues and feature branches.
Issues are used to keep track of everything that happens in the source code. The main purpose of an Issue is to explain the purpose of the change. Depending on the nature of the change, a description of the change should also be added. For example, an Issue for a bug can have a description that explains the cause of the problem, and suggest a solution.

Issues shall be done by implementing the needed changes in a branch. Once the branch is fully developed it can be merged into the master branch. As a member you will be able to create and push you own branches. 

### 3.2 Merging to master, and Pull Requests
A branch can only be made through a Pull Request (since it will require certain approvals).
A Pull Request created by a member can not be merged to master without the approval of the  owner of the code (see the CODEOWNERS file). In order to achieve this, create a Pull Request for your branch once it's ready, and invite the code owner and other members that needs to review it. The branch can be merged to master once it's successfully reviewed.

The following are guidelines to follow as a member and contributor of Loadcoder:

* All changes shall be preceded by a describing Issue in order for you and others to understand what the purpose is with the change. In order to map the branch to the corresponding Issue, add the Issue ID (#[number]) in the name of the branch you are working in, similar to any of the following:

```
bugfix_#3_memory-leak-in-chart
feature_#3_new-cool-chart-colors
```
Also state the Issue you are working with in the git commit messages.

```
git commit -m "#3 Fix memory leak in the chart functionality" ChartFunctionality.java
```
* Keep the users in focus and keep things simple. Loadcoder's user APIs must be kept understandable.

* Write unittests for your functional changes, and make sure everything builds and all tests passes.

* Document your changes. Comment the code and write javadocs if needed. Also suggest documentation for [loadcoder.com](http://loadcoder.com/documentation.html) as Issues in [https://github.com/loadcoder/loadcoder-site](https://github.com/loadcoder/loadcoder-site)



## 4.  Contributors
Thanks to the following developers who have contributed code to this class library:

Stefan Vahlgren (Author)

## 5. Contact
Email: info@loadcoder.com

## 6.  License

Loadcoder is licensed under the terms of the GNU GENERAL PUBLIC LICENSE Version 3 or later.

###### 6.1 Copyright Notice
Copyright (C) 2018 Stefan Vahlgren at Loadcoder

