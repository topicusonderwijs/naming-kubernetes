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
		
		publishCoverageReports { }

		recordIssues aggregatingResults: true, tools: [spotBugs(useRankAsPriority: true)]
		
//		publishAnalysisReports {
//			aggregate = false
//			findbugs = false
//			jacoco = true
//		}
	}

	notify { }
}