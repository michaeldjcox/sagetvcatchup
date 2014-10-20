package Test

import uk.co.mdjcox.sagetv.model.Programme

String link = "/programme?id=testProgramme;type=html"
Programme programme = new Programme(
        source.getId(),
        "testProgramme",
        "Test Programme",
        "Test Programme",
        link,
        "/logo.png",
        ""
);
programme.addMetaUrl(link);

programmes.add(programme);







