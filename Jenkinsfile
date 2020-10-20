@Library('jenkins-pipeline-library@task-warnings-ng-plugin') _

config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

	stage("Reports") {
		publishTestReports { }

		publishAnalysisReports {
			aggregate = false
			findbugs = false
			jacoco = true
		}
	}

	notify { }
}