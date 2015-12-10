jQuery.fn.getOptionsFromAttributes = function () {
    var options = {};

    if (this.length) {

        jQuery.each(this[0].attributes, function () {

            var map,
                nodeValue = this.nodeValue,
                target = options;

            if (/^data-/.test(this.nodeName)) {

                map = this.nodeName.replace(/^data-/, "").split("."),

                AJS.$.each(map, function (i, propertyName) {

                    propertyName = propertyName.replace(/([a-z])-([a-z])/gi, function (entireMatch, firstMatch, secondMatch) {
                        return firstMatch + secondMatch.toUpperCase();
                    });

                    propertyName = propertyName.replace(/_([a-z]+)/gi, function (entireMatch, firstMatch) {
                        return firstMatch.toUpperCase();
                    });

                    if (i === map.length-1) {
                        target[propertyName] = nodeValue.match(/^(tru|fals)e$/i) ? nodeValue.toLowerCase() == "true" : nodeValue;
                    } else if (!target[propertyName]) {
                        target[propertyName] = {};
                    }
                    target = target[propertyName];
                });

            }
        });
    }
    
    return options;
};
