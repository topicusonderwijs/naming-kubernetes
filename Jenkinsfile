@Library('jenkins-pipeline-library@task-warnings-ng-plugin') _

config { numberToKeep=2 }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
			publishIssues = false
		}
	}

	reportIssues(
		profile: 'java-jacoco'
	)

	notify { }
}