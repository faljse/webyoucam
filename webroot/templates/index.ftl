<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hello Bulma!</title>
    <link rel="stylesheet" href="static/bulma.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="/static/js/jsmpeg.min.js"></script>
</head>
<body>
<section class="section">
    <div class="container">
        <h1 class="title">
            WebYouCam
        </h1>
           <#list cmd as c>
                <li>${c?index+1}</li>
        <canvas id="videoCanvas_${c?index+1}" width="640" height="480">
            <p>
                Please use a browser that supports the Canvas Element, like
                <a href="http://www.google.com/chrome">Chrome</a>,
                <a href="http://www.mozilla.com/firefox/">Firefox</a>,
                <a href="http://www.apple.com/safari/">Safari</a> or Internet Explorer 10
            </p>
        </canvas>
        <script type="text/javascript">
            // Setup the WebSocket connection and start the player
            function url(s) {
                var l = window.location;
                return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + l.pathname + s;
            }
            //var client = new WebSocket( url('stream/output') );
            var canvas_${c?index+1} = document.getElementById('videoCanvas_${c?index+1}');


            var player_${c?index+1} = new JSMpeg.Player(url('stream/output/${c?index+1}'), {canvas:canvas_${c?index+1}});
        </script>
           </#list>
    </div>
</section>
</body>
</html>