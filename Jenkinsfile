config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

	publishTestReports { }

	notify { }
}