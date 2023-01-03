config { }

node("build02) {
	git.checkout { }

	catchError {
		maven {
			goals = 'deploy'
		}
	}

	reportIssues(
		profiles: 'java-jacoco'
	)

	notify {
		emailNotificationRecipients = 'Sverre.Boschman@topicus.nl'
	}
}
