package Iplayer

String url = "http://www.bbc.co.uk/iplayer/group/most-popular";

String mostPopularDetails = GET_WEB_PAGE(url, stopFlag);

mostPopularDetails = MOVE_TO("<li class=\"list-item episode numbered\"", mostPopularDetails);

List<String> mostPopularList = new ArrayList<String>();

while (mostPopularDetails != null) {
    mostPopularDetails = MOVE_TO("href=\"", mostPopularDetails);
    String mostPopular = EXTRACT_TO("\"", mostPopularDetails);
    if (mostPopular != null && !mostPopular.isEmpty()) {
        mostPopular = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", mostPopular);
        mostPopularList.add(mostPopular);
    }
}

if (!mostPopularList.isEmpty()) {
    categories.put("Most Popular", mostPopularList);
}





