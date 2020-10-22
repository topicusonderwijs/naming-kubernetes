config { }

node(){
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

	reportIssues(
		profiles: 'java-jacoco'
	)

	notify { }
}