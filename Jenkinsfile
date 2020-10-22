@Library('jenkins-pipeline-library@task-warnings-ng-plugin') _

config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
			publishIssues = false
		}
	}

	reportIssues(
		profiles: 'java-jacoco'
	)

	notify { }
}