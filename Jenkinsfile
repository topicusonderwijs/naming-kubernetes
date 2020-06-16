config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

	publishTestReports { }

  publishAnalysisReports {
    aggregate = false
    jacoco = true
  }

	notify { }
}