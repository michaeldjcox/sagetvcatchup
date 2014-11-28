package Test

String link = "/episode?id=TestEpisode;type=html";
episode.addMetaUrl(link);
episode.setEpisodeTitle("Test Episode");
episode.setSeriesTitle("Test Series");
episode.setDescription("Test Episode Description");
episode.setIconUrl("http://ichef.bbci.co.uk/images/ic/272x153/p01l9hsm.jpg");
episode.setServiceUrl("/watch?id=TestEpisode;type=mpeg4");
episode.setChannel("Test");
episode.addGenre("Factual");
episode.addGenre("News")
episode.setSeries("1");
episode.setEpisode("1");
episode.setAirDate("01-09-2014")
episode.setAirTime("10:00:00");
episode.setOrigAirDate("01-10-2014")
episode.setOrigAirTime("12:00:00");
episode.setDuration("60");
episode.setId("TestEpisode")
return episode;
