// currently not used, keeping it future reference - https://developer.atlassian.com/confdev/confluence-plugin-guide/confluence-plugin-module-types/macro-module/including-information-in-your-macro-for-the-macro-browser
var jsOverrides = {
    "fields" : {
        "string" : {
            "ssoToken" : function(params,options){
                var field = AJS.MacroBrowser.ParameterFields["_hidden"](params, options);
                field.setValue("someHiddenValue");
                return field;
            }       
         }
    }
};
AJS.MacroBrowser.setMacroJsOverride("if-sso", jsOverrides);
