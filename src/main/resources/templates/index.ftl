<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, shrink-to-fit=no, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>WebYouCam</title>
    <script src="static/js/jsmpeg.min.js"></script>
</head>
<body>
<div id="wrapper">
    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-12">
                    <canvas id="videoCanvas" width="640" height="480">
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
                        var canvas = document.getElementById('videoCanvas');
                        var player = new JSMpeg.Player(url('stream/output/1'), {canvas:canvas});
                    </script>
                </div>
            </div>
        </div>
    </div>
    <!-- /#page-content-wrapper -->

</div>
<!-- /#wrapper -->
</body>
</html>