
jQuery.fn.avataror = function (options) {
    var $ = jQuery,
        $document = $(document);
    this.each(function () {
        var $this = $(this);

        var imgsrc = $this.find("img").attr("src");
        $this.css({"-moz-border-radius": "10px", "-webkit-border-radius": "10px"});
        $this.html("<p>Loading?</p>");
        var avataror = {previewSize: 48};
        avataror.preview = $("<div/>").addClass("avataror-preview").css({border: "solid 1px #000", "float": "left", height: avataror.previewSize +"px", overflow: "hidden", width: avataror.previewSize +"px", position: "relative", top: "-9999em", left: "-9999em"});
        avataror.preview.prependTo(options.previewElement);
        avataror.img = $('<img src="' + imgsrc + '" alt="Avatar Source"/>');
        avataror.img.load(function () {
            avataror.image = $("<div/>").css({background: "url('" + imgsrc + "') no-repeat", clear: "left", position: "relative"});
            avataror.marker = $("<div/>").css({cursor: "move", position: "relative" });
            avataror.dash = $("<div/>");
            avataror.shadow = $("<div/>");
            avataror.dash.add(avataror.shadow).css({cursor: "move", opacity: .5, left: 0, top: 0, position: "absolute"});
            avataror.image.append(avataror.shadow).append(avataror.dash).append(avataror.marker);
            $this.append(avataror.image);
            avataror.marker.html('<div></div><div></div><div></div><div></div>');
            $("div", avataror.marker).each(function (i) {
                var $this = $(this);
                $this.css({background: "#000", border: "solid 1px #fff", width: "10px", height: "10px", position: "absolute", "font-size": "1px"});
                $this.css(["left", "right", "right", "left"][i], "-6px");
                $this.css(["top", "top", "bottom", "bottom"][i], "-6px");
                $this.css("cursor", ["nw-resize", "ne-resize", "se-resize", "sw-resize"][i]);
                $this.mousedown(function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    avataror.dragging = {x: e.pageX, y: e.pageY, ax: avataror.x, ay: avataror.y, w: avataror.width, h: avataror.height, i: i + 1};
                    avataror.shadow.hide();
                });
            });
            avataror.marker.add(avataror.image).mousedown(function (e) {
                e.preventDefault();
                avataror.dragging = {
                    x: e.pageX,
                    y: e.pageY,
                    ax: avataror.x,
                    ay: avataror.y,
                    w: avataror.width,
                    h: avataror.height};
                avataror.shadow.hide();
            });

            $document.mouseup(function (e) {
                avataror.handleMouseUp(e);
            });
            $document.mousemove(function (e) {
                if (avataror.dragging) {
                    avataror.handleMouseMove(e.pageX, e.pageY);
                    e.preventDefault();
                }
            });


            avataror.imgwidth = avataror.img.width();
            avataror.imgheight = avataror.img.height();
            avataror.x = parseInt($("#avatar-offsetX").val());
            avataror.y = parseInt($("#avatar-offsetY").val());
            avataror.width = parseInt($("#avatar-width").val());
            avataror.height = avataror.width;
            avataror.image.css({width: avataror.imgwidth + "px", height: avataror.imgheight + "px"});
            avataror.setMarker();

            $this.css({width: avataror.imgwidth + "px"});
            avataror.preview.css({position: "static"});
            $("p", $this).remove();
            $this.trigger("AvatarImageLoaded");
        });
        avataror.preview.append(avataror.img);

        avataror.setMarker = function () {
            avataror.marker.css("border", "dashed 1px #fff");
            avataror.dash.css("border", "solid 1px #000");
            avataror.shadow.css("border", "solid 1px #000");
            avataror.marker.add(this.dash).css("left", this.x - 1 + "px");
            avataror.marker.add(avataror.dash).css("top", avataror.y - 1 + "px");
            avataror.shadow.css("border-left-width", avataror.x + "px");
            avataror.shadow.css("border-right-width", avataror.imgwidth - avataror.x - avataror.width + "px");
            avataror.shadow.css("border-top-width", avataror.y + "px");
            avataror.shadow.css("border-bottom-width", avataror.imgheight - avataror.y - avataror.height + "px");
            avataror.shadow.css("width", avataror.width + "px");
            avataror.shadow.css("height", avataror.height + "px");
            avataror.marker.add(avataror.dash).css("width", avataror.width + "px");
            avataror.marker.add(avataror.dash).css("height", avataror.height + "px");
        };

        avataror.adjustPreview = function() {
            avataror.img.attr("width", avataror.imgwidth * avataror.previewSize / avataror.width);
            avataror.img.attr("height", avataror.imgheight * avataror.previewSize / avataror.height);
            avataror.img.css("margin-left", "-" + avataror.x * avataror.previewSize / avataror.width + "px");
            avataror.img.css("margin-top", "-" + avataror.y * avataror.previewSize / avataror.height + "px");
            avataror.preview.select();
        };

        avataror.handleMouseMove = function(newX, newY) {
            avataror.dragging.nextExec = avataror.dragging.nextExec || 0;
            if (avataror.dragging.nextExec == 0) {
                avataror.dragging.nextExec = 3;
            } else {
                avataror.dragging.nextExec--;
                return;
            }
            var dx = newX - avataror.dragging.x;
            var dy = newY - avataror.dragging.y;
            if (this.dragging.i) {
                var handler = avataror.resizeHandlers[this.dragging.i-1];
                handler(dx,dy);
            } else {
                avataror.x = avataror.dragging.ax + dx;
                avataror.y = avataror.dragging.ay + dy;
                if (avataror.x + avataror.width > avataror.imgwidth) {
                    avataror.x = avataror.imgwidth - avataror.width;
                }
                if (avataror.y + avataror.height > avataror.imgheight) {
                    avataror.y = avataror.imgheight - avataror.height;
                }
                if (avataror.x < 0) {
                    avataror.x = 0;
                }
                if (avataror.y < 0) {
                    avataror.y = 0;
                }
            }
            avataror.setMarker();
            avataror.adjustPreview();
        };

        avataror.handleMouseUp = function(e) {
//            avataror.adjustPreview();
            $("#avatar-offsetX").val(avataror.x);
            $("#avatar-offsetY").val(avataror.y);
            $("#avatar-width").val(avataror.width);
            avataror.dragging = null;
            avataror.shadow.show();
        };

        avataror.originX = function() {
            return avataror.dragging.ax;
        };
        avataror.originY = function() {
            return avataror.dragging.ay;
        };
        avataror.originBottomX = function() {
            return avataror.dragging.ax + avataror.dragging.w;
        };
        avataror.originBottomY = function() {
            return avataror.dragging.ay + avataror.dragging.h;
        };

        avataror.originNw = function() {
            return {x: avataror.originX(), y: avataror.originY()};
        };
        avataror.originNe = function() {
            return {x: avataror.originBottomX(), y: avataror.originY()};
        };
        avataror.originSe = function() {
            return {x: avataror.originBottomX(), y: avataror.originBottomY()};
        };
        avataror.originSw = function() {
            return {x: avataror.originX(), y: avataror.originBottomY()};
        };

        avataror.nwHandler = function(dx, dy) {
            var anchor = avataror.originSe();
            var tmpBase = {x: avataror.originX() + dx, y: avataror.originY() + dy};
            var diffX = Math.abs(tmpBase.x - anchor.x), diffY = Math.abs(tmpBase.y - anchor.y);
            var newSize = Math.min(diffX, diffY);
            if (newSize < 20) {
                newSize = 20;
            }
            if (anchor.x - newSize < 0) {
                newSize = anchor.x;
            }
            if (anchor.y - newSize < 0) {
                newSize = anchor.y;
            }
            avataror.x = anchor.x - newSize;
            avataror.y = anchor.y - newSize;
            avataror.width = avataror.height = newSize;
        };

        avataror.neHandler = function(dx, dy) {
            var anchor = avataror.originSw();
            var tmpBase = {x: avataror.originBottomX() + dx, y: avataror.originY() + dy};
            var diffX = Math.abs(tmpBase.x - anchor.x), diffY = Math.abs(tmpBase.y - anchor.y);
            var newSize = Math.min(diffX, diffY);

            if (newSize < 20) {
                newSize = 20;
            }
            if (anchor.x + newSize > avataror.imgwidth) {
                newSize = avataror.imgwidth - anchor.x;
            }
            if (anchor.y - newSize < 0) {
                newSize = anchor.y;
            }

            avataror.y = anchor.y - newSize;
            avataror.width = avataror.height = newSize;
        };

        avataror.seHandler = function(dx, dy) {
            var anchor = avataror.originNw();
            var tmpBase = {x: avataror.originBottomX() + dx, y: avataror.originBottomY() + dy};
            var diffX = Math.abs(tmpBase.x - anchor.x), diffY = Math.abs(tmpBase.y - anchor.y);
            var newSize = Math.min(diffX, diffY);

            if (newSize < 20) {
                newSize = 20;
            }
            if (anchor.x + newSize > avataror.imgwidth) {
                newSize = avataror.imgwidth - anchor.x;
            }
            if (anchor.y + newSize > avataror.imgheight) {
                newSize = avataror.imgheight - anchor.y;
            }
            avataror.width = avataror.height = newSize;
        };

        avataror.swHandler = function(dx, dy) {
            var anchor = avataror.originNe();
            var tmpBase = {x: avataror.originX() + dx, y: avataror.originBottomY() + dy};
            var diffX = Math.abs(tmpBase.x - anchor.x), diffY = Math.abs(tmpBase.y - anchor.y);
            var newSize = Math.min(diffX, diffY);

            if (newSize < 20) {
                newSize = 20;
            }
            if (anchor.x - newSize < 0) {
                newSize = anchor.x;
            }
            if (anchor.y + newSize > avataror.imgheight) {
                newSize = avataror.imgheight - anchor.y;
            }
            avataror.x = anchor.x - newSize;
            avataror.width = avataror.height = newSize;
        };

        avataror.resizeHandlers = [avataror.nwHandler, avataror.neHandler, avataror.seHandler, avataror.swHandler];

        // implementation
    });
};
