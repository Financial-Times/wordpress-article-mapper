@BodyProcessing
Feature: Body processing links

  Scenario Outline:
    Given I have body <internal link html>
    When I transform it
    Then it is transformed, <internal link html> becomes <transformed link html>

  Examples:
    | internal link html                                                               | transformed link html                                                                                                                                    |
    | <p>Good stories <a href="http://on.ft.com/1NVIQzo">link to</a> other stories</p> | <p>Good stories <content id="8adad508-077b-3795-8569-18e532cabf96" type="http://www.ft.com/ontology/content/Article">link to</content> other stories</p> |


  Scenario Outline:
    Given I have body <external link html>
    When I transform it
    Then it is left unmodified

  Examples:
    | external link html                                                                                    |
    | <p>Better stories <a href="http://example.com/fascinating-insights.html">link to other</a> sites</p>  |

