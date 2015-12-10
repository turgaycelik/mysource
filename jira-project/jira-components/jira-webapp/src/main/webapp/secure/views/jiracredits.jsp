<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'about.jira.name'"/></title>
    <meta name="decorator" content="panel-general" />
</head>
<body>
<script>
jQuery(function () {
    var bullets = [],
            bulletSpeed = 1000, // pixels per second
            canFire = true,
            credits = jQuery("#credits"),
            keys = {},
            lastLoop = new Date(),
            movementSpeed = 750, // pixels per second
            onResize,
            ship = jQuery("#version-num"),
            shipContents = ship.find("span > span > span"),
            shipPixelSize = parseInt(ship.children().css("font-size"), 10),
            shipSize = getShipSize(),
            startedAt;

    function cleanUp () {
        jQuery(document).off("keydown", onKeyDown);
        jQuery(document).off("keyup", onKeyUp);
        jQuery(window).off("resize", onResize);
    }

    /**
     * Spawn a bullet at the paddle's current position.
     */
    function fire () {
        var bullet = jQuery("<div/>").addClass("bullet").appendTo("#stage"),
                bulletHeight = bullet.outerHeight(true),
                bulletWidth = bullet.outerWidth(true),
                duration,
                shipOffset = getShipOffset();

        if (!startedAt) {
            startedAt = new Date();
        }

        bullet.css({
            top: shipOffset.top - bulletHeight,
            left: shipOffset.left + shipSize.width / 2 - bulletWidth / 2
        });

        duration = (bullet.position().top + bulletHeight) / bulletSpeed * 1000;
        bullet.animate({top: -bulletHeight}, duration, "linear", function () {
            removeBullet(bullet);
        });

        bullets.push(bullet);
    }

    /**
     * Check if any bullets are intersecting targets.
     */
    function hitTest () {
        var targets = jQuery(".crew li:not(.hit)");
        if (targets.length === 0) {
            showFinishedMessage();
            return;
        }

        _.each(bullets, function (bullet) {
            // Bail early if it's nowhere near the targets.
            if (!isIntersecting(bullet, credits)) {
                return;
            }

            targets.each(function () {
                var target = jQuery(this);
                if (isIntersecting(bullet, target)) {
                    removeBullet(bullet);
                    target.addClass("hit");
                    return false;
                }
            });
        });
    }

    /**
     * Determine if two elements are intersecting (ignoring margins).
     *
     * @param {jQuery} a The first element.
     * @param {jQuery} b The second element.
     * @returns {boolean} true iff the elements are intersecting.
     */
    function isIntersecting (a, b) {
        var aHeight = a.outerHeight(),
                aHidden = a.css("visibility") === "hidden",
                aOffset = a.offset(),
                aWidth = a.outerWidth(),
                bHeight = b.outerHeight(),
                bHidden = b.css("visibility") === "hidden",
                bOffset = b.offset(),
                bWidth = b.outerWidth();

        if (aHidden || bHidden) {
            return false;
        }

        return !(
                aOffset.left > bOffset.left + bWidth ||
                        bOffset.left > aOffset.left + aWidth ||
                        aOffset.top > bOffset.top + bHeight ||
                        bOffset.top > aOffset.top + aHeight
                );
    }

    /**
     * The game's main loop, run as fast as the browser can manage.
     */
    function loop () {
        var closed = ship.closest("body").length < 1,
                delta = (new Date() - lastLoop) / 1000,
                LEFT = 37,
                RIGHT = 39,
                SPACE = 32;

        if (closed) {
            cleanUp();
            return;
        }

        if (keys[LEFT]) {
            moveShip(-movementSpeed * delta);
        }

        if (keys[RIGHT]) {
            moveShip(movementSpeed * delta);
        }

        if (keys[SPACE]) {
            canFire && fire();
            canFire = false;
        } else {
            canFire = true;
        }

        hitTest();

        _.defer(loop);
        lastLoop = new Date();
    }

    /**
     * Move the ship along the X axis.
     *
     * @param {number} delta The distance to move the ship, in pixels.
     */
    function moveShip (delta) {
        var maximum = jQuery("#stage").outerWidth(true) - shipSize.width + shipPixelSize;
        position = parseFloat(ship.css("left"));

        ship.css("left", Math.max(shipPixelSize, Math.min(maximum, position + delta)));
    }

    function onKeyDown (e) {
        console.log("onKeyDown");
        keys[e.which] = true;
    }

    function onKeyUp (e) {
        keys[e.which] = false;
    }

    onResize = _.bind(moveShip, this, 0);

    /**
     * Remove a bullet from the game.
     *
     * @param {jQuery} bullet The bullet to remove.
     */
    function removeBullet (bullet) {
        bullet.remove();
        bullets = _.without(bullets, bullet);
    }

    function getShipOffset () {
        return {
            left: ship.position().left - shipPixelSize,
            top: ship.position().top - shipPixelSize * 2
        };
    }

    function getShipSize () {
        return {
            height: shipContents.outerHeight() + shipPixelSize * 2,
            width: shipContents.outerWidth() + shipPixelSize * 2
        };
    }

    function showFinishedMessage () {
        var finished = jQuery("#finished"),
                time = ((new Date()) - startedAt) / 1000;

        if (finished.hasClass("visible")) {
            return;
        }

        finished.addClass("visible");
        finished.find("span").text(time.toFixed(0));
    }

    jQuery(document).on("keydown", onKeyDown);
    jQuery(document).on("keyup", onKeyUp);
    jQuery(window).on("resize", onResize);
    loop();

    _.defer(function () {
        jQuery(document.activeElement).blur();
    })
});
</script>
<style type="text/css">
#finished {
    display: none;
}
#finished.visible {
    color: #f2f2f2;
    display: block;
    font-size: 16px;
    left: 8em;
    line-height: 2;
    position: absolute;
    text-align: center;
    top: 7em;
    width: 75%;
}
#finished p {
    font-size: 10px;
}

.bullet {
    background: #fff;
    display: block;
    height: 20px;
    position: absolute;
    width: 6px;
}

#stage {
    font-size: 16px;
    width: 1024px;
    height: 570px;
    background-color: #000;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
    padding: 3em 2em 6em;
    position: relative;
    overflow: hidden;
}

#stage:before {
    color: #f2f2f2;
    display: block;
    content: "HIGH SCORE: ";
    position: absolute;
    top: 1em;
    left: 1em;
}

#version-num {
    position: absolute;
    bottom: 30px;
    left: 0;
    text-align: center;
}

#version-num > span {
    font-size: 6px;
    display: block;
    background-color: #ddd;
    width:1em;
    height: 1em;
    box-shadow:
        5.25em -2.25em 0 0 #ddd,
        4.75em -1.5em 0 0 #ddd,
        5.65em -1.5em 0 0 #ddd,
        -1em 0 0 0 #ddd,
        -1em 1em 0 0 #ddd,
        11.5em 0 0 0 #ddd,
        11.5em 1em 0 0 #ddd;
}

#version-num > span > span > span {
    background-color: #ddd;
    display: block;
    font-size: 10px;
    padding: .25em .5em;
    position: relative;
    top:-.33em;
    width: 6em;
}

#stage h3 {
    position: absolute;
    bottom:70px;
    left: 0;
    width: 80%;
    margin: 0 15%;
}
#stage h3 span {
    display: inline-block;
    text-align: center;
    width: 100px;
    height: 2.5em;
    color: yellow;
    background-color: yellow;
    margin-left:15%;
    position: relative;
    text-shadow: 0 3px 6px #999;
}
#stage h3 span:first-child {
    margin-left: 0;
}
    /* 8-bit */
#stage h3 span:before {
    position: absolute;
    content: "";
    height: 1em;
    width: 1em;
    box-shadow:
        -2em -0.5em 0 0 yellow,
        -1em -0.5em 0 0 yellow,
        0 -0.5em 0 0 yellow,
        1em -0.5em 0 0 yellow,
        2em -0.5em 0 0 yellow,
        -1em -1em 0 0 yellow,
        0 -1em 0 0 yellow,
        1em -1em 0 0 yellow,
        0 -1.5em 0 0 yellow,
        -.5em 1.5em 0 0 #000,
        -1.5em 2em 0 0 #000,
        0.5em 1.5em 0 0 #000,
        1.5em 2em 0 0 #000 ;
}

    /* smooth */
    /*#stage h3 span:before {*/
    /*position: absolute;*/
    /*content: "";*/
    /*top:-30px;*/
    /*left: 0;*/
    /*border-bottom: 30px solid yellow;*/
    /*border-left: 30px solid transparent;*/
    /*border-right: 30px solid transparent;*/
    /*height: 0;*/
    /*width: 40px;*/
    /*}*/

    /*#stage h3 span:after {*/
    /*position: absolute;*/
    /*content: "";*/
    /*top:1.5em;*/
    /*left: 2em;*/
    /*height: 2em;*/
    /*width: 2em;*/
    /*background-color: #000;*/
    /*border-radius: 2em;*/
    /*}*/

.crew {
    display: inline;
    font-size: 5px;
    margin: 0;
    padding: 0;
    list-style: none;
}
.crew li {
    display: inline-block;
    margin: 0 .5em 1em;
    z-index: 1;
}
.crew li.hit {
    visibility: hidden;
}
.crew li strong {
    display: inline-block;
    width: 7em;
    height: 7em;
    overflow: hidden;
}
.crew li span {
    display: inline-block;
    position: relative;
    z-index: 2;
    background: #57AFE5;
    width:1em;
    height:1em;
    text-indent: -999em;
    margin: 2em 3em;
    box-shadow:
        /* dance */
        0 -1.75em 0 0 #9FC71C,
        -.25em -2em 0 0 #9FC71C,
        .25em -2em 0 0 #9FC71C,

        .75em .75em 0 0 #57AFE5,
        1.25em 1.25em 0 0 #57AFE5,
        1.75em 2em 0 0 #57AFE5,
        2.25em 3em 0 0 #57AFE5,

        -1.25em 1.5em 0 0 #57AFE5,
        -1.75em 2em 0 0 #57AFE5,
        -2.25em 3em 0 0 #57AFE5,

        .85em -.75em 0 0 #57AFE5,
        1.55em -1.25em 0 0 #57AFE5,
        2.25em -2em 0 0 #57AFE5,

        -.85em -.75em 0 0 #57AFE5,
        -1.55em -1.25em 0 0 #57AFE5,
        -2.25em -2em 0 0 #57AFE5,

            /* like no one is watching */
        -12em -1.75em 0 0 #9FC71C,
        -12.25em -2em 0 0 #9FC71C,
        -11.85em -2em 0 0 #9FC71C,

        -12em 0 0 0 #57AFE5,
        -11.55em .75em 0 0 #57AFE5,
        -10.95em 1.25em 0 0 #57AFE5,
        -10.55em 2em 0 0 #57AFE5,
        -10.95em 3em 0 0 #57AFE5,

        -13.25em 1.5em 0 0 #57AFE5,
        -13.75em 2em 0 0 #57AFE5,
        -13.25em 3em 0 0 #57AFE5,

        -11.25em -.75em 0 0 #57AFE5,
        -10.75em -1.25em 0 0 #57AFE5,
        -9.95em -1em 0 0 #57AFE5,

        -12.85em -.75em 0 0 #57AFE5,
        -13.55em -1.25em 0 0 #57AFE5,
        -14.25em -1em 0 0 #57AFE5;

}
.crew li span.MoD {
    background: #F276EC;
    box-shadow:
        /* dance */
        0 -1.75em 0 0 #9FC71C,
        -.25em -2em 0 0 #9FC71C,
        .25em -2em 0 0 #9FC71C,

        .75em .75em 0 0 #F276EC,
        1.25em 1.25em 0 0 #F276EC,
        1.75em 2em 0 0 #F276EC,
        2.25em 3em 0 0 #F276EC,

        -1.25em 1.5em 0 0 #F276EC,
        -1.75em 2em 0 0 #F276EC,
        -2.25em 3em 0 0 #F276EC,

        .85em -.75em 0 0 #F276EC,
        1.55em -1.25em 0 0 #F276EC,
        2.25em -2em 0 0 #F276EC,

        -.85em -.75em 0 0 #F276EC,
        -1.55em -1.25em 0 0 #F276EC,
        -2.25em -2em 0 0 #F276EC,

            /* like no one is watching */
        -12em -1.75em 0 0 #9FC71C,
        -12.25em -2em 0 0 #9FC71C,
        -11.85em -2em 0 0 #9FC71C,

        -12em 0 0 0 #F276EC,
        -11.55em .75em 0 0 #F276EC,
        -10.95em 1.25em 0 0 #F276EC,
        -10.55em 2em 0 0 #F276EC,
        -10.95em 3em 0 0 #F276EC,

        -13.25em 1.5em 0 0 #F276EC,
        -13.75em 2em 0 0 #F276EC,
        -13.25em 3em 0 0 #F276EC,

        -11.25em -.75em 0 0 #F276EC,
        -10.75em -1.25em 0 0 #F276EC,
        -9.95em -1em 0 0 #F276EC,

        -12.85em -.75em 0 0 #F276EC,
        -13.55em -1.25em 0 0 #F276EC,
        -14.25em -1em 0 0 #F276EC;

}

.crew li[data-details]:hover:after {
    background-color: #000;
    z-index: 3;
    text-indent: 0;
    content: attr(data-details);
    padding: 8px;
    font-size: 16px;
    color: #fff;
    position: absolute;
    top: .5em;
    left: 13em;
    white-space: nowrap;
}

@-webkit-keyframes handsup {
    0% {left: 0;}
    50% {left: 12em;}
    100% {left: 0;}
}
@-moz-keyframes handsup {
    0% {left: 0;}
    50% {left: 12em;}
    100% {left: 0;}
}
@-o-keyframes handsup {
    0% {left: 0;}
    50% {left: 12em;}
    100% {left: 0;}
}
@-ms-keyframes handsup {
    0% {left: 0;}
    50% {left: 12em;}
    100% {left: 0;}
}
@keyframes handsup {
    0% {left: 0;}
    50% {left: 12em;}
    100% {left: 0;}
}

.crew li span {
    -webkit-transform: translateZ(0);
    -moz-transform: translateZ(0);
    -ms-transform: translateZ(0);
    -o-transform: translateZ(0);
    transform: translateZ(0);

    -webkit-animation: handsup 1.5s step-start infinite;
    -moz-animation: handsup 1.5s step-start infinite;
    -o-animation: handsup 1.5s step-start infinite;
    -ms-animation: handsup 1.5s step-start infinite;
    animation: handsup 1.5s step-start infinite;
}
    /* removed the animation of invader charlie across/down the screen - performance hit to browsers was too big */

</style>
<link href='//fonts.googleapis.com/css?family=Press+Start+2P' rel='stylesheet' type='text/css'>
<style type='text/css'>
    #stage {
        font-family: 'Press Start 2P', sans-serif;
    }
</style>
<div id="stage">
    <h2>JIRA&nbsp;<ww:property value="/buildVersion"/>&nbsp;Credits</h2>
    <h3><span id="J">J</span><span id="I">I</span><span id="R">R</span><span id="A">A</span></h3>
    <div href="#" id="version-num">
        <span><span><span><ww:property value="/buildVersion"/></span></span></span>
    </div>

    <div id="credits">
        <ol class="crew" id="one">
            <li data-details="Joshua Ali &mdash; Graduate Java Developer"><strong><span>Joshua Ali &mdash; Graduate Java Developer</span></strong></li>
            <li data-details="Ignat Alexeyenko &mdash; Software Engineer"><strong><span>Ignat Alexeyenko &mdash; Software Engineer</span></strong></li>
            <li data-details="Jaiden Ashmore &mdash; Developer"><strong><span>Jaiden Ashmore &mdash; Developer</span></strong></li>
            <li data-details="Bradley Ayers &mdash; Developer"><strong><span>Bradley Ayers &mdash; Developer</span></strong></li>
            <li data-details="Brenden Bain &mdash; Senior Java Developer"><strong><span>Brenden Bain &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Brad Baker &mdash; Development Team Lead"><strong><span>Brad Baker &mdash; Development Team Lead</span></strong></li>
            <li data-details="Veenu Bharara &mdash; QA Engineer"><strong><span>Veenu Bharara &mdash; QA Engineer</span></strong></li>
            <li data-details="Pawel Bugalski &mdash; Developer"><strong><span>Pawel Bugalski &mdash; Developer</span></strong></li>
            <li data-details="Antoine B&uuml;sch &mdash; Senior Java Developer"><strong><span>Antoine B&uuml;sch &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Trevor Campbell &mdash; Senior Java Developer"><strong><span>Trevor Campbell &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Ross Chaldecott &mdash; Designer"><strong><span>Ross Chaldecott &mdash; Designer</span></strong></li>
            <li data-details="Panna Cherukuri &mdash; QA Engineer"><strong><span>Panna Cherukuri &mdash; QA Engineer</span></strong></li>
            <li data-details="Sergio Cinos &mdash; JavaScript Developer"><strong><span>Sergio Cinos &mdash; JavaScript Developer</span></strong></li>
            <li data-details="Joanne Cranford &mdash; Java Front End Developer"><strong><span>Joanne Cranford &mdash; Java Front End Developer</span></strong></li>
            <li data-details="Jonathon Creenaune &mdash; Development Team Lead"><strong><span>Jonathon Creenaune &mdash; Development Team Lead</span></strong></li>
            <li data-details="Sean Curtis &mdash; Frontend Developer"><strong><span>Sean Curtis &mdash; Frontend Developer</span></strong></li>
            <li data-details="Eric Dalgliesh &mdash; Bugmaster/Developer"><strong><span>Eric Dalgliesh &mdash; Bugmaster/Developer</span></strong></li>
            <li data-details="Chris Darroch &mdash; Frontend Developer"><strong><span>Chris Darroch &mdash; Frontend Developer</span></strong></li>
            <li data-details="Gilmore Davidson &mdash; JS Developer"><strong><span>Gilmore Davidson &mdash; JS Developer</span></strong></li>
            <li data-details="Jeroen De Raedt &mdash; Java Developer"><strong><span>Jeroen De Raedt &mdash; Java Developer</span></strong></li>
            <li data-details="Josh Devenny &mdash; Product Manager"><strong><span>Josh Devenny &mdash; Product Manager</span></strong></li>
            <li data-details="Christopher Doble &mdash; Graduate Java Developer"><strong><span>Christopher Doble &mdash; Graduate Java Developer</span></strong></li>
            <li data-details="George El Boustani &mdash; Developer"><strong><span>George El Boustani &mdash; Developer</span></strong></li>
            <li data-details="Michael Elias &mdash; Senior Java Developer"><strong><span>Michael Elias &mdash; Senior Java Developer</span></strong></li>
        </ol>
        <ol class="crew" id="two">
            <li data-details="Dave Elkan &mdash; JS Developer"><strong><span>Dave Elkan &mdash; JS Developer</span></strong></li>
            <li data-details="Judd Garratt &mdash; UX Designer"><strong><span>Judd Garratt &mdash; UX Designer</span></strong></li>
            <li data-details="Chris Fuller &mdash; Senior Java Developer"><strong><span>Chris Fuller &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Slawek Ginter &mdash; Senior Java Developer"><strong><span>Slawek Ginter &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Susan Griffin &mdash; Technical Writer"><strong><span>Susan Griffin &mdash; Technical Writer</span></strong></li>
            <li data-details="Ian Grunert &mdash; Java Developer"><strong><span>Ian Grunert &mdash; Java Developer</span></strong></li>
            <li data-details="Shihab Hamid &mdash; Development Team Lead"><strong><span>Shihab Hamid &mdash; Development Team Lead</span></strong></li>
            <li data-details="Joshua Hansen &mdash; Graduate Java Developer"><strong><span>Joshua Hansen &mdash; Graduate Java Developer</span></strong></li>
            <li data-details="Scott Harwood &mdash; Senior JS Developer"><strong><span>Scott Harwood &mdash; Senior JS Developer</span></strong></li>
            <li data-details="James Hazelwood &mdash; Graduate Developer"><strong><span>James Hazelwood &mdash; Graduate Developer</span></strong></li>
            <li data-details="Martin Henderson &mdash; Senior Software Developer"><strong><span>Martin Henderson &mdash; Senior Software Developer</span></strong></li>
            <li data-details="Oswaldo Hernandez &mdash; Java Developer"><strong><span>Oswaldo Hernandez &mdash; Java Developer</span></strong></li>
            <li data-details="Simone Houghton &mdash; Program Manager"><strong><span>Simone Houghton &mdash; Program Manager</span></strong></li>
            <li data-details="Adam Jakubowski &mdash; JIRA Developer"><strong><span>Adam Jakubowski &mdash; JIRA Developer</span></strong></li>
            <li data-details="Rosie Jameson &mdash; Technical Writer"><strong><span>Rosie Jameson &mdash; Technical Writer</span></strong></li>
            <li data-details="Martin Jopson &mdash; Frontend Developer"><strong><span>Martin Jopson &mdash; Frontend Developer</span></strong></li>
            <li data-details="Justin Koke &mdash; Team Lead"><strong><span>Justin Koke &mdash; Team Lead</span></strong></li>
            <li data-details="Dariusz Kordonski &mdash; Java Developer"><strong><span>Dariusz Kordonski &mdash; Java Developer</span></strong></li>
            <li data-details="Markus Kramer &mdash; Developer"><strong><span>Markus Kramer &mdash; Developer</span></strong></li>
            <li data-details="Roy Krishna &mdash; Product Manager"><strong><span>Roy Krishna &mdash; Product Manager</span></strong></li>
            <li data-details="Jimmy Kurniawan &mdash; Graduate Java Developer"><strong><span>Jimmy Kurniawan &mdash; Graduate Java Developer</span></strong></li>
            <li data-details="Mark Lassau &mdash; Development Team Lead"><strong><span>Mark Lassau &mdash; Development Team Lead</span></strong></li>
            <li data-details="Zehua Liu &mdash; Developer"><strong><span>Zehua Liu &mdash; Developer</span></strong></li>
            <li data-details="Andrew Lui &mdash; Team Lead - Technical Writer"><strong><span>Andrew Lui &mdash; Team Lead - Technical Writer</span></strong></li>
            <li data-details="Alex Manusu &mdash; Product Management Intern"><strong><span>Alex Manusu &mdash; Product Management Intern</span></strong></li>
        </ol>
        <ol class="crew" id="three">
            <li data-details="Anund McKague &mdash; Graduate Developer"><strong><span>Anund McKague &mdash; Graduate Developer</span></strong></li>
            <li data-details="Martin Meinhold &mdash; Java Developer"><strong><span>Martin Meinhold &mdash; Java Developer</span></strong></li>
            <li data-details="Nick Menere &mdash; Development Team Lead"><strong><span>Nick Menere &mdash; Development Team Lead</span></strong></li>
            <li data-details="Aleksander Mierzwicki &mdash; Developer"><strong><span>Aleksander Mierzwicki &mdash; Developer</span></strong></li>
            <li data-details="Luis Miranda &mdash; Senior Developer"><strong><span>Luis Miranda &mdash; Senior Developer</span></strong></li>
            <li data-details="Chris Mountford &mdash; Senior Software Developer"><strong><span>Chris Mountford &mdash; Senior Software Developer</span></strong></li>
            <li data-details="Pawel Niewiadomski &mdash; Senior Java Developer"><strong><span>Pawel Niewiadomski &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Maciej Nowakowski &mdash; Developer"><strong><span>Maciej Nowakowski &mdash; Developer</span></strong></li>
            <li data-details="Peter Obara &mdash; QA Engineer"><strong><span>Peter Obara &mdash; QA Engineer</span></strong></li>
            <li data-details="Mairead O'Donovan &mdash; Product Manager"><strong><span class="MoD">Mairead O'Donovan &mdash; Product Manager</span></strong></li>
            <li data-details="Ken Olofsen &mdash; Marketing Manager"><strong><span>Ken Olofsen &mdash; Marketing Manager</span></strong></li>
            <li data-details="Michal Orzechowski &mdash; Java Developer"><strong><span>Michal Orzechowski &mdash; Java Developer</span></strong></li>
            <li data-details="Justus Pendleton &mdash; Development Team Lead"><strong><span>Justus Pendleton &mdash; Development Team Lead</span></strong></li>
            <li data-details="Matt Quail &mdash; Product Architect"><strong><span>Matt Quail &mdash; Product Architect</span></strong></li>
            <li data-details="Jonathan Raoult &mdash; Senior Java Developer"><strong><span>Jonathan Raoult &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Becc Roach &mdash; User Experience Designer"><strong><span>Becc Roach &mdash; User Experience Designer</span></strong></li>
            <li data-details="Filip Rogaczewski &mdash; Developer"><strong><span>Filip Rogaczewski &mdash; Developer</span></strong></li>
            <li data-details="Jay Rogers &mdash; UI Designer"><strong><span>Jay Rogers &mdash; UI Designer</span></strong></li>
            <li data-details="Bryan Rollins &mdash; Group Product Manager"><strong><span>Bryan Rollins &mdash; Group Product Manager</span></strong></li>
            <li data-details="Michael Ruflin &mdash; Developer"><strong><span>Michael Ruflin &mdash; Developer</span></strong></li>
            <li data-details="Wojciech Seliga &mdash; Development Team Lead"><strong><span>Wojciech Seliga &mdash; Development Team Lead</span></strong></li>
            <li data-details="Mike Sharp &mdash; Design Engineer"><strong><span>Mike Sharp &mdash; Design Engineer</span></strong></li>
        </ol>
        <ol class="crew" id="four">
            <li data-details="Kiran Shekhar &mdash; QA Engineer"><strong><span>Kiran Shekhar &mdash; QA Engineer</span></strong></li>
            <li data-details="Pawel Skierczynski &mdash; Senior Java Developer"><strong><span>Pawel Skierczynski &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Paul Slade &mdash; Manager JIRA"><strong><span>Paul Slade &mdash; Manager JIRA</span></strong></li>
            <li data-details="Robert Smart &mdash; Senior Java Developer"><strong><span>Robert Smart &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Graeme Smith &mdash; Java Developer"><strong><span>Graeme Smith &mdash; Java Developer</span></strong></li>
            <li data-details="Andrew Swan &mdash; Senior Java Developer"><strong><span>Andrew Swan &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Maciej Swinarski &mdash; Developer"><strong><span>Maciej Swinarski &mdash; Developer</span></strong></li>
            <li data-details="David Tang &mdash; Graduate JS Developer"><strong><span>David Tang &mdash; Graduate JS Developer</span></strong></li>
            <li data-details="Roman Tekhov &mdash; Senior Java Developer"><strong><span>Roman Tekhov &mdash; Senior Java Developer</span></strong></li>
            <li data-details="Samantha Thebridge &mdash; User Interaction Designer"><strong><span>Samantha Thebridge &mdash; User Interaction Designer</span></strong></li>
            <li data-details="Michael Tokar &mdash; Developer"><strong><span>Michael Tokar &mdash; Developer</span></strong></li>
            <li data-details="Michael Truong &mdash; Student Developer"><strong><span>Michael Truong &mdash; Student Developer</span></strong></li>
            <li data-details="Wojciech Urbanski &mdash; Front-end Developer"><strong><span>Wojciech Urbanski &mdash; Front-end Developer</span></strong></li>
            <li data-details="James Winters &mdash; Development Team Lead"><strong><span>James Winters &mdash; Development Team Lead</span></strong></li>
            <li data-details="Lukasz Wlodarczyk &mdash; Developer"><strong><span>Lukasz Wlodarczyk &mdash; Developer</span></strong></li>
            <li data-details="Ben Wong &mdash; JS Developer"><strong><span>Ben Wong &mdash; JS Developer</span></strong></li>
            <li data-details="Geoffrey Wong &mdash; Graduate QA Engineer"><strong><span>Geoffrey Wong &mdash; Graduate QA Engineer</span></strong></li>
            <li data-details="Penny Wyatt &mdash; QA Team Lead"><strong><span>Penny Wyatt &mdash; QA Team Lead</span></strong></li>
            <li data-details="Michal Zeglarski &mdash; Developer"><strong><span>Michal Zeglarski &mdash; Developer</span></strong></li>
            <li data-details="Edward Zhang &mdash; Java Developer"><strong><span>Edward Zhang &mdash; Java Developer</span></strong></li>
            <li data-details="Jaime Sanchez &mdash; Java Developer"><strong><span>Jaime Sanchez &mdash; Java Developer</span></strong></li>
            <li data-details="Sergio Cia &mdash; QA Engineer"><strong><span>Sergio Cia &mdash; QA Engineer</span></strong></li>
            <li data-details="Norman Atashbar &mdash; Java Developer"><strong><span>Norman Atashbar &mdash; Java Developer</span></strong></li>
            <li data-details="Grzegorz Tanczyk &mdash; Java Developer"><strong><span>Grzegorz Tanczyk &mdash; Java Developer</span></strong></li>
            <li data-details="Piotr Klimkowski &mdash; Java Developer"><strong><span>Piotr Klimkowski &mdash; Java Developer</span></strong></li>
            <li data-details="Arkadiusz Glowacki &mdash; Java Developer"><strong><span>Arkadiusz Glowacki &mdash; Java Developer</span></strong></li>
        </ol>
        <ol class="crew" id="five">
            <li data-details="Abdoulaye Kindy Diallo &mdash; support"><strong><span>Abdoulaye Kindy Diallo &mdash; support</span></strong></li>
            <li data-details="Ahmad Danial &mdash; support"><strong><span>Ahmad Danial &mdash; support</span></strong></li>
            <li data-details="Ahmad Faisal &mdash; support"><strong><span>Ahmad Faisal &mdash; support</span></strong></li>
            <li data-details="Alex Conde &mdash; support"><strong><span>Alex Conde &mdash; support</span></strong></li>
            <li data-details="Amalia Sanusi &mdash; support"><strong><span>Amalia Sanusi &mdash; support</span></strong></li>
            <li data-details="Amanda Wei San Nan &mdash; support"><strong><span>Amanda Wei San Nan &mdash; support</span></strong></li>
            <li data-details="Andre Quadros Petry &mdash; support"><strong><span>Andre Quadros Petry &mdash; support</span></strong></li>
            <li data-details="Azwandi Mohd Aris &mdash; support"><strong><span>Azwandi Mohd Aris &mdash; support</span></strong></li>
            <li data-details="Bastiaan Jansen &mdash; support"><strong><span>Bastiaan Jansen &mdash; support</span></strong></li>
            <li data-details="Boris Berenberg &mdash; support"><strong><span>Boris Berenberg &mdash; support</span></strong></li>
            <li data-details="Bruno Rosa &mdash; support"><strong><span>Bruno Rosa &mdash; support</span></strong></li>
            <li data-details="Chan Chung Park &mdash; support"><strong><span>Chan Chung Park &mdash; support</span></strong></li>
            <li data-details="Chin Kim Loong &mdash; support"><strong><span>Chin Kim Loong &mdash; support</span></strong></li>
            <li data-details="Chris Le Petit &mdash; support"><strong><span>Chris Le Petit &mdash; support</span></strong></li>
            <li data-details="Christopher Shim &mdash; support"><strong><span>Christopher Shim &mdash; support</span></strong></li>
            <li data-details="Clarissa Gauterio &mdash; support"><strong><span>Clarissa Gauterio &mdash; support</span></strong></li>
            <li data-details="Daniel Leng &mdash; support"><strong><span>Daniel Leng &mdash; support</span></strong></li>
            <li data-details="Daryl Chua &mdash; support"><strong><span>Daryl Chua &mdash; support</span></strong></li>
            <li data-details="David Chan &mdash; support"><strong><span>David Chan &mdash; support</span></strong></li>
            <li data-details="David Currie &mdash; support"><strong><span>David Currie &mdash; support</span></strong></li>
            <li data-details="David Mason &mdash; support"><strong><span>David Mason &mdash; support</span></strong></li>
            <li data-details="David Nicholson &mdash; support"><strong><span>David Nicholson &mdash; support</span></strong></li>
            <li data-details="Dora Wierzbicka &mdash; support"><strong><span>Dora Wierzbicka &mdash; support</span></strong></li>
            <li data-details="Eric Kieling &mdash; support"><strong><span>Eric Kieling &mdash; support</span></strong></li>
            <li data-details="Felipe Willig &mdash; support"><strong><span>Felipe Willig &mdash; support</span></strong></li>
            <li data-details="Foogie Sim &mdash; support"><strong><span>Foogie Sim &mdash; support</span></strong></li>
            <li data-details="Gary Sackett &mdash; support"><strong><span>Gary Sackett &mdash; support</span></strong></li>
            <li data-details="Guilherme Nedel &mdash; support"><strong><span>Guilherme Nedel &mdash; support</span></strong></li>
            <li data-details="Hanis Suhailah &mdash; support"><strong><span>Hanis Suhailah &mdash; support</span></strong></li>
            <li data-details="Immanuel Siagian &mdash; support"><strong><span>Immanuel Siagian &mdash; support</span></strong></li>
            <li data-details="Ivan Maduro &mdash; support"><strong><span>Ivan Maduro &mdash; support</span></strong></li>
            <li data-details="Ivan Tse &mdash; support"><strong><span>Ivan Tse &mdash; support</span></strong></li>
            <li data-details="Janet Albion &mdash; support"><strong><span>Janet Albion &mdash; support</span></strong></li>
            <li data-details="Jason Worley &mdash; support"><strong><span>Jason Worley &mdash; support</span></strong></li>
            <li data-details="Jeff Curry &mdash; support"><strong><span>Jeff Curry &mdash; support</span></strong></li>
            <li data-details="Jeison Spaniol &mdash; support"><strong><span>Jeison Spaniol &mdash; support</span></strong></li>
            <li data-details="Joachim Ooi &mdash; support"><strong><span>Joachim Ooi &mdash; support</span></strong></li>
            <li data-details="Joe Wai &mdash; support"><strong><span>Joe Wai &mdash; support</span></strong></li>
            <li data-details="John Garcia &mdash; support"><strong><span>John Garcia &mdash; support</span></strong></li>
            <li data-details="John Inder &mdash; support"><strong><span>John Inder &mdash; support</span></strong></li>
            <li data-details="Jorge Dias &mdash; support"><strong><span>Jorge Dias &mdash; support</span></strong></li>
            <li data-details="Lucas Lima &mdash; support"><strong><span>Lucas Lima &mdash; support</span></strong></li>
            <li data-details="Lucas Timm &mdash; support"><strong><span>Lucas Timm &mdash; support</span></strong></li>
            <li data-details="Marcus Almeida &mdash; support"><strong><span>Marcus Almeida &mdash; support</span></strong></li>
            <li data-details="Marlon Aguiar &mdash; support"><strong><span>Marlon Aguiar &mdash; support</span></strong></li>
            <li data-details="Matheus Fernandes &mdash; support"><strong><span>Matheus Fernandes &mdash; support</span></strong></li>
            <li data-details="Matthew Hunter &mdash; support"><strong><span>Matthew Hunter &mdash; support</span></strong></li>
            <li data-details="Michael Andreacchio &mdash; support"><strong><span>Michael Andreacchio &mdash; support</span></strong></li>
            <li data-details="Michael Knight &mdash; support"><strong><span>Michael Knight &mdash; support</span></strong></li>
            <li data-details="Mick Nassette &mdash; support"><strong><span>Mick Nassette &mdash; support</span></strong></li>
            <li data-details="Miranda Rawson &mdash; support"><strong><span>Miranda Rawson &mdash; support</span></strong></li>
            <li data-details="Muhammad Fahd &mdash; support"><strong><span>Muhammad Fahd &mdash; support</span></strong></li>
            <li data-details="Nick Mason &mdash; support"><strong><span>Nick Mason &mdash; support</span></strong></li>
            <li data-details="Omar Ahmed Al-Safi &mdash; support"><strong><span>Omar Ahmed Al-Safi &mdash; support</span></strong></li>
            <li data-details="Osman Afridi &mdash; support"><strong><span>Osman Afridi &mdash; support</span></strong></li>
            <li data-details="Paul Greig &mdash; support"><strong><span>Paul Greig &mdash; support</span></strong></li>
            <li data-details="Pedro Cora &mdash; support"><strong><span>Pedro Cora &mdash; support</span></strong></li>
            <li data-details="Pelle Kirkeby &mdash; support"><strong><span>Pelle Kirkeby &mdash; support</span></strong></li>
            <li data-details="Peter Koczan &mdash; support"><strong><span>Peter Koczan &mdash; support</span></strong></li>
            <li data-details="Pietro Schaff &mdash; support"><strong><span>Pietro Schaff &mdash; support</span></strong></li>
            <li data-details="Razaq Omar &mdash; support"><strong><span>Razaq Omar &mdash; support</span></strong></li>
            <li data-details="Renjith Pillai &mdash; support &amp; bugfix"><strong><span>Renjith Pillai &mdash; support &amp; bugfix</span></strong></li>
            <li data-details="Rian Josua Masikome &mdash; support"><strong><span>Rian Josua Masikome &mdash; support</span></strong></li>
            <li data-details="Rick Bal &mdash; support"><strong><span>Rick Bal &mdash; support</span></strong></li>
            <li data-details="Ruchi Tandon &mdash; support"><strong><span>Ruchi Tandon &mdash; support</span></strong></li>
            <li data-details="Septa Cahyadiputra &mdash; support"><strong><span>Septa Cahyadiputra &mdash; support</span></strong></li>
            <li data-details="Teck En Yew &mdash; support"><strong><span>Teck En Yew &mdash; support</span></strong></li>
            <li data-details="Theodore Tzidamis &mdash; support"><strong><span>Theodore Tzidamis &mdash; support</span></strong></li>
            <li data-details="Tiago Kolling Comasseto &mdash; support"><strong><span>Tiago Kolling Comasseto &mdash; support</span></strong></li>
            <li data-details="Turner Benard &mdash; support"><strong><span>Turner Benard &mdash; support</span></strong></li>
            <li data-details="Tyler Davis &mdash; support"><strong><span>Tyler Davis &mdash; support</span></strong></li>
            <li data-details="Vicky Kharisma &mdash; support"><strong><span>Vicky Kharisma &mdash; support</span></strong></li>
            <li data-details="Voon Kiat Gan &mdash; support"><strong><span>Voon Kiat Gan &mdash; support</span></strong></li>
            <li data-details="Yilin Mo &mdash; support"><strong><span>Yilin Mo &mdash; support</span></strong></li>
            <li data-details="Zulfadli Noor Sazali &mdash; support"><strong><span>Zulfadli Noor Sazali &mdash; support</span></strong></li>
        </ol>
    </div>
    <div id="finished">Congratulations, you debugged the JIRA team in <span></span> seconds!
        <p>( now get back to work ;p )</p>
    </div>
</div>
</body>
</html>
