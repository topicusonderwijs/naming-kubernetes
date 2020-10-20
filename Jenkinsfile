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
		publishAnalysisIssues()
	
		//publishTestReports { } // not warnings-ng-plugin
		
		//publishCoverageReports { }  // not warnings-ng-plugin

		//recordIssues aggregatingResults: true, tools: [spotBugs(useRankAsPriority: true), junitParser(pattern: '**/target/surefire-reports/*.xml')]
		
//		publishAnalysisReports {
//			aggregate = false
//			findbugs = false
//			jacoco = true
//		}
	}

	notify { }
}