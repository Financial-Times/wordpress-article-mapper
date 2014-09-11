<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Content Model Viewer</title>

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <style>
        #readout {
            margin-top: 0.5em;
            margin-bottom: 0.5em;
            padding: 0.25em;
            font-family: monospace;
        }

        #code .well { font-family: monospace; }
        #code .key {
            color: red;
        }

        #code .string {
            color: green;
        }

        #preview .well {
            background-image: none;
            background: #fff1e0;
        }
    </style>

  </head>
  <body>

    <div class="container">
        <h1>View ${appName} Content</h1>

        <div class="input-group">
          <span class="input-group-addon">#</span>
          <input id="postId" type="text" class="form-control" placeholder="Post ID">
          <span class="input-group-btn">
            <button class="btn btn-default" id="view" type="button">View</button>
          </span>
        </div>

        <p id="readout" class="text-info bg-info">

        </p>

        <!-- Nav tabs -->
        <ul class="nav nav-tabs" role="tablist">
          <li class="active"><a href="#code" role="tab" data-toggle="tab">JSON</a></li>
          <li><a href="#preview" role="tab" data-toggle="tab">Content Preview</a></li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
          <div class="tab-pane active" id="code">
            <pre class="well">{ ...  JSON ... }</pre>

          </div>
          <div class="tab-pane" id="preview">
            <div class="well">
                <h1></h1>
                <div id="bodyPreview">

                </div>
            </div>
          </div>
        </div>
        <p><a id="permalink" href="#">#0000</a></p>

    </div>






    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>

    <script>
        var pathTemplate = "${modelUriTemplate}";
        $("#readout").hide();

        function displayResult(jqXHR, url) {

            var jsonResult = JSON.parse(jqXHR.responseText);
            var htmlShowingJson = syntaxHighlight(jsonResult);

            $("#code .well").html(htmlShowingJson);

            $("#readout").removeClass("text-info bg-info text-warning bg-warning");
            if(jqXHR.status==200) {
                $("#readout").addClass("text-info bg-info");
                $("#preview h1").text(jsonResult.title);
                $("#bodyPreview").html(jsonResult.body);
            } else {
                $("#readout").addClass("text-warning bg-warning");
                $("#preview h1").text(jsonResult.message);
                $("#bodyPreview").text("NO TEXT");
            }

            $("#readout").text((new Date()) + ": " + url);

            $("#readout").show();
        }

        function loadData() {

            var postId = $("#postId").val();
            var origin = document.location.origin;

            var path = pathTemplate.replace("{{ID}}",postId);
            var url = origin+path;

            var result = $.get( url ).done(function(rawJson) {
                displayResult(result, url);
            }).fail(function(thing) {
              displayResult(result, url);
            });

            var permaHref = "#" + postId;
            $("#permalink").text(permaHref).attr("href",permaHref);
            document.location.hash=permaHref;

        }

        $(document).ready(function() {

            if(document.location.hash!="") {
                $("#postId").val(document.location.hash.substring(1));
                loadData();
            }

            $("#view").click(loadData);
        });

        // http://stackoverflow.com/questions/4810841/how-can-i-pretty-print-json-using-javascript
        function syntaxHighlight(json) {
            if (typeof json != 'string') {
                 json = JSON.stringify(json, undefined, 2);
            }
            json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
                var cls = 'number';
                if (/^"/.test(match)) {
                    if (/:$/.test(match)) {
                        cls = 'key';
                    } else {
                        cls = 'string';
                    }
                } else if (/true|false/.test(match)) {
                    cls = 'boolean';
                } else if (/null/.test(match)) {
                    cls = 'null';
                }
                return '<span class="' + cls + '">' + match + '</span>';
            });
        }

    </script>


  </body>
</html>