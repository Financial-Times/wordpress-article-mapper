@BodyProcessing
Feature: Body processing

  This shows how particular tags are processed.

  For details of how tags will look before and after for particular processing rules, see bodyprocessingrule.feature.

  Scenario Outline:
    Given a replacement tag <replacement> and the fastFt body contains <tagname> the transformer will TRANSFORM THE TAG

  Examples: Transform to html5-compliant tags
    | tagname | replacement |
    | b       | strong      |
    | i       | em          |
    | s       | del         |
    | strike  | del         |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will RETAIN ELEMENT AND REMOVE ATTRIBUTES

  Examples: Tidy up tags to remove attributes that probably relate to formatting
    | tagname |
    | strong  |
    | em      |
    | sub     |
    | sup     |
    | h1      |
    | h2      |
    | h3      |
    | h4      |
    | h5      |
    | h6      |
    | ol      |
    | ul      |
    | li      |
    | del     |
    | u       |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will STRIP ELEMENT AND CONTENTS

  Examples: Remove tags completely, including content, for html5 tags that we cannot support currently
    | tagname  |
    | applet   |
    | audio    |
    | base     |
    | basefont |
    | button   |
    | canvas   |
    | caption  |
    | col      |
    | colgroup |
    | command  |
    | datalist |
    | dir      |
    | embed    |
    | fieldset |
    | form     |
    | frame    |
    | frameset |
    | head     |
    | input    |
    | keygen   |
    | label    |
    | legend   |
    | link     |
    | map      |
    | menu     |
    | meta     |
    | nav      |
    | noframes |
    | noscript |
    | object   |
    | optgroup |
    | option   |
    | output   |
    | param    |
    | progress |
    | rp       |
    | rt       |
    | ruby     |
    | script   |
    | select   |
    | source   |
    | style    |
    | table    |
    | tbody    |
    | td       |
    | textarea |
    | tfoot    |
    | th       |
    | thead    |
    | tr       |
    | track    |
    | video    |
    | wbr      |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname | html                                           |
    | !--     | <!-- comments -->                              |
    | weird   | <weird>text surrounded by unknown tags</weird> |
    | code    | <code>Why would they ever use code?</code>     |


  Scenario Outline: Remove inline assets
    Given I have body <with inline asset>
    When I transform it
    Then I get the body <without inline asset>

  Examples:
    | with inline asset                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | without inline asset                                                                   |
    | <p>Confusion:</p><div class="morevideo"><a href="http://video.ft.com/">More video</a></div>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | <p>Confusion:</p>                                                                      |
    | <blockquote><p>This is a fine quote. Cometh the man, cometh the hour.</p></blockquote>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | <blockquote><p>This is a fine quote. Cometh the man, cometh the hour.</p></blockquote> |

  Scenario Outline: Fix markup problems
    Given I have body <with errors>
    When I transform it
    Then I get the body <without errors>

  Examples:
    | with errors                                                       | without errors                                       |
    | <p>a paragraph <span>with complex and incorrect</p> markup</span> | <p>a paragraph with complex and incorrect</p> markup |
    | <p>a paragraph with <br> line break</p>                           | <p>a paragraph with <br/> line break</p>             |

@TagSoup
  Scenario Outline: Remove formatting tags around tweets
      Given I have body <unformatted>
      When I transform it
      Then I get the body <formatted>

  Examples:
    | unformatted                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | formatted                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
    | <div data-asset-type="embed"><blockquote class="twitter-tweet" lang="en"><p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting <a href="https://twitter.com/search?q=%24DTV&amp;src=ctag">$DTV</a> shareholders from decline in <a href="https://twitter.com/search?q=%24T&amp;src=ctag">$T</a> stock. (Caps upside, too).</p>— Liz Hoffman (@lizrhoffman) <a href="https://twitter.com/lizrhoffman/statuses/468146880682016769">May 18, 2014</a></blockquote><script src="//platform.twitter.com/widgets.js" charset="utf-8"></script></div> | <blockquote class="twitter-tweet" lang="en"><p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting <a href="https://twitter.com/search?q=%24DTV&amp;src=ctag">$DTV</a> shareholders from decline in <a href="https://twitter.com/search?q=%24T&amp;src=ctag">$T</a> stock. (Caps upside, too).</p>— Liz Hoffman (@lizrhoffman) <a href="https://twitter.com/lizrhoffman/statuses/468146880682016769">May 18, 2014</a></blockquote>|

