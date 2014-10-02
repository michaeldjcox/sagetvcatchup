package Test

String link = "http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/testEpisode"
episode.addMetaUrl(link);
episode.setEpisodeTitle("Test Episode");
episode.setSeriesTitle("Test Series");
episode.setDescription("Test Episode Description");
episode.setIconUrl("http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/logo.png");
episode.setServiceUrl("http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/watch?id=TestEpisode");
episode.setChannel("Test");
episode.addGenre("Factual");
episode.addGenre("News")
episode.setSeries("1");
episode.setEpisode("1");
episode.setAirDate("2014-09-01")
episode.setAirTime("10:00");
episode.setId("TestEpisode")
return episode;
