// noConflict
if (window.__require) {
    window.require = window.__require;
    window.requirejs = window.__requirejs;
    window.define = window.__define;
} else {
    // Patch our own version of Almond.
    //
    // If "define.amd" is truthy, some 3rd-party libs (e.g. jQuery, spin.js)
    // automatically register themselves via define(). We don't want that,
    // we'll take care of calling define() for each lib.
    delete window.define.amd;
}

// IE8 doesn't support delete window.?
try { delete window.__require; } catch (e) { window.__require = undefined; }
try { delete window.__requirejs; } catch (e) { window.__requirejs = undefined; }
try { delete window.__define; } catch (e) { window.__define = undefined; }
