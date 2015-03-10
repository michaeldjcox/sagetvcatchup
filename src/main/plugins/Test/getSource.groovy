package Test

import uk.co.mdjcox.sagetv.model.Source

Source source = new Source();
source.setSourceId("Test");
source.setId("Catchup/Sources/Test");
source.setShortName("Test");
source.setLongName("Test");
source.setServiceUrl("/category?id=test;type=html");

sources.add(source);