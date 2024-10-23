# Virtual Scroll Access System

This is a Spring Boot + Thymeleaf web application that allows users to upload and download scrolls.
This app also features admin features for managing users and content uploaded.


## Table of Contents

- [Getting Started](#getting-started)
  - [Set up the Repository](#set-up-the-repository)
  - [Running the Project](#running-the-project)
- [Testing the Program](#testing-the-program)
- [Contributing to the Project](#contributing-to-the-project)


## Getting Started 

Ensure the following is installed on your machine:
- Java SDK 17+
- Gradle

### Set up the Repository

```bash
git clone  https://github.sydney.edu.au/SOFT2412-COMP9412-2024Sem2/Behroz_Lab01_Group01_CE_A1.git
```

Then build the project using Gradle:

```bash
gradle clean build
```

### Running the Project
Then run the main program from the root directory of the project:

```bash
gradle run
```

The application will start and allow you to access it locally at http://localhost:8080 which allows for users to sign up and upload/download scrolls.


#### Configuration
`http://localhost:8080/h2-console`: This url is springboots database manager that is allowed locally. Using the sql settings in `/resources/application.properties` you can login and change the flag on a user account to set the admin flag to give access to the admin dashboards and features.

## Testing the Program

We use `JaCoCo` for test coverage reports. The `jacocoTestReport` task is configured to run automatically after the `test` task. It generates the test coverage report in the `build/reports/jacoco/test/html/` directory. To test, navigate to the root directory and run:

```bash
./gradlew build
```

To view the coverage report, open the index.html file located in `build/reports/jacoco/test/html/`.

## Contributing to the Project

We welcome any contributions to our project. If you want to contribute:

1. Fork the repository on GitHub.
2. Clone your forked repository onto your local machine.
3. Create a new branch for your added work:
```bash
git checkout -b <branch_name>
```
4. Commit your changes.
5. Push to your forked repository:
```bash
git push origin <branch_name>
```
6. Create a pull request on GitHub.

Please use the GitHub issue tracker to report any bugs or feature requests.
