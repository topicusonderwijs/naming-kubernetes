config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

  stage("Reports") {}
    publishTestReports { }

    publishAnalysisReports {
      aggregate = false
      jacoco = true
    }
  }

	notify { }
}