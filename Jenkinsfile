config { }

node() {
	git.checkout { }

	catchError {
		maven {
			image = 'docker.topicus.education/jenkins/jenkins-maven-build:3-corretto-debian'
			goals = 'deploy'
		}
	}

	reportIssues(
		profiles: 'java-jacoco'
	)

	notify {
		slackChannel = "#dev-cobra"
	}
}
