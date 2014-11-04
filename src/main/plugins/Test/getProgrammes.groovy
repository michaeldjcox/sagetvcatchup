package Test

import uk.co.mdjcox.sagetv.model.Programme

String link = "/programme?id=testProgramme;type=html"
Programme programme = new Programme(
        source.getId(),
        "testProgramme",
        "Test Programme",
        "Test Programme",
        link,
        "http://upload.wikimedia.org/wikipedia/en/0/02/The_Amazing_Spider-Man_theatrical_poster.jpeg",
        ""
);
programme.addMetaUrl(link);

programmes.add(programme);







