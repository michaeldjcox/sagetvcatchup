package Test

import uk.co.mdjcox.sagetv.model.Programme

String link = "http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/programme?id=testProgramme;type=html"
Programme programme = new Programme(
        source.getId(),
        "testProgramme",
        "Test Programme",
        "Test Programme",
        link,
        "http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/logo.png",
        ""
);
programme.addMetaUrl(link);

programmes.add(programme);







