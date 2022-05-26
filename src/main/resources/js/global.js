// this script will modify certain checkboxes' value and visibility on the Scroll Versions Publish dialog, remove the "- PRIVATE" from the target space name, and change the space key and name based on the selected version
AJS.toInit(function(){
    var buttons = document.getElementsByClassName("sv-publish");
    for (i = 0; i < buttons.length; i++) {
        buttons[i].addEventListener("click",
            function() {
                var checkExist = setInterval(function() {
                    $("#remoteSystemId").parent().hide();
                    $("#copyLabels").parent().hide();
                    $("#keepAuthorsAndDates").parent().hide();
                    $("#removeAttachments").parent().hide();
                    $("#sendEmail").parent().hide();
                    var checkboxes = [];
                    checkboxes[0] = document.getElementById("copyLabels");
                    checkboxes[1] = document.getElementById("keepAuthorsAndDates");
                    checkboxes[2] = document.getElementById("removeAttachments");
                    checkboxes[3] = document.getElementById("onlyPagesInFinalState");
                    checkboxes[4] = document.getElementById("onlyPagesInFinalStateProcessChildren");
                    for (i = 0; i < checkboxes.length; i++) {
                        if (checkboxes[i] !== null) {
                            checkboxes[i].value = "true";
                            checkboxes[i].checked = true;
                            clearInterval(checkExist);
                        }
                        if (checkboxes[i].id === "onlyPagesInFinalStateProcessChildren") {
                            var parent = checkboxes[i].parentNode;
                            checkboxes[i].checked = false;
                            parent.style.display = "block";
                        }
                    }
                     var targetSpaceName = document.getElementById("targetSpaceName");
                     if (targetSpaceName) {
                         var newName = targetSpaceName.value.replace(/-\sPRIVATE\s*/g, "");
                         targetSpaceName.value = newName;  
                     }
                     var targetSpaceKey = document.getElementById("targetSpaceKey");
                     var versionSelect = document.getElementById("versionId");
                     var currentVersionId = versionSelect.value;
                     var sv_infoline = document.getElementById("sv-infoline");
                     var versionContextRaw = sv_infoline.getAttribute("data-scroll-versions-context");
                     var versionContext = JSON.parse(versionContextRaw);
                     var versionsArray = versionContext.versions;
                     versionSelect.addEventListener("change", function() {
                         var newVersionId = versionSelect.options[versionSelect.selectedIndex].value;
                         var currentVersion = versionsArray.filter(function(data){
                             return data.versionId === currentVersionId;
                         });
                         var newVersion = versionsArray.filter(function(data){
                             return data.versionId === newVersionId;
                         });
                         var currentVersionName = currentVersion[0].name;
                         var newVersionName = newVersion[0].name;
                         var currentSpaceKeyVersion = currentVersionName.replace(/[^a-zA-Z0-9]/g, "");
                         var newSpaceKeyVersion = newVersionName.replace(/[^a-zA-Z0-9]/g, "");
                         targetSpaceKey.value = targetSpaceKey.value.replace(currentSpaceKeyVersion, newSpaceKeyVersion);
                         targetSpaceName.value = targetSpaceName.value.replace(currentVersionName, newVersionName);
                         currentVersionId = newVersionId;
                     });
                }, 200); // check every 200ms
            }
        );
    };
});
 
// This will remove the email settings Subscribe to all blog posts, Show changed content and Subscribe to recommended updates 

AJS.toInit(function(){
  AJS.$("input[name='siteBlogWatchForUser']").parent().parent().css("display", "none");
  AJS.$("input[name='showDiffInEmailNotifications']").parent().parent().css("display", "none");
  AJS.$("input[name='receiveRecommendedEmail']").parent().parent().css("display", "none");
  //hide the Edit button in the User Profile page
  AJS.$("a[href='/users/editmyprofile.action']").hide();
 if (window.location.pathname === "/dopeopledirectorysearch.action" || window.location.pathname === "/browsepeople.action") { AJS.$("#main").html("<a href='https://infoscribe.infoway-inforoute.ca'>Back to InfoScribe Home</a>"); };
});

// <!-- Modifies the header bar to include a button that links to the InfoScribe Help and Customization space  -->

// NOTE: when using this code in production, remove the "/confluence/" from each of the links
AJS.toInit(function(){
    var helpMenu = AJS.$('ul#help-menu-link-leading');
    helpMenu.children().hide();
    helpMenu.prepend('<li><a href="/display/HELP/Getting+Started" class=" aui-nav-imagelink" title="Getting Started">Getting Started</a></li>');
    var keyboardShortcut = AJS.$(helpMenu).find('a#keyboard-shortcuts-link').parent('li');
    keyboardShortcut.show();
    helpMenu.append('<li><a href="/display/HELP/Video+Tutorials" class=" aui-nav-imagelink" title="Video Tutorials">Video Tutorials</a></li><li><a href="/display/HELP/FAQ" class=" aui-nav-imagelink" title="FAQ">FAQ</a></li><li><a href="/display/HELP/Contact+Us" class=" aui-nav-imagelink" title="Contact Us">Contact Us</a></li>');
});

// load the latest Scroll Versions for the current space if the current selected version is "Currently Published"
$(function() {
    var workingVersionSelector = $("div[id='sv-working-version']");
    //checks if there is a "Working Version" dropdown
    if (workingVersionSelector.length) {
        var workingVersionId = workingVersionSelector.attr("data-version-id");
        var currentPageId = $("meta[name='ajs-page-id']").attr("content");
        //checks if it is set to "currently published"
        if (workingVersionId === "current") {
            //need to find latest version and switch to it
            var allVersionObjs = $("div#sv-working-version").find("a.item-link.sv-version");
            var versionArray = [];
            var allVersions = {};
            allVersionObjs.each(function(index, obj) {
                //obj is the DOM object, whereas $(this) is the jQuery object
                //store version as key, and version id as value
                var version = $(this).attr("data-name");
                versionArray.push(version);
                allVersions[version] = $(this).attr("data-version-id");
            });
            var lastIndex = versionArray.length - 1;
            var newest = versionArray[lastIndex];
            //with the latest version, and page id, create a link to redirect to
            var newLink = AJS.params.baseUrl + "/rest/scroll-versions/latest/versions/user?workingVersionId=" + allVersions[newest] + "&pageId=" + currentPageId;
            document.location.href = newLink;
        }
    }
});

//only show certain spaces in the SubSpace Menu
$(function() {
	$(".subspaces").children().children().hide();
	$(".subspaces .level_0 li.space-CRR").show();
	$(".subspaces .level_0 li.space-BRR").show();
	$(".subspaces .level_0 li.space-SS").show();
	$(".subspaces .level_0 li.space-ST").show();
	$(".subspaces .level_0 li.space-SR").show();
	$("#subspace-global-navigation").show();
});

//hide the user menu for the 'guest' account to prevent the guest from modify the guest settings
AJS.toInit(function(){
	if (AJS.params.remoteUser === "guest") { AJS.$("#user-menu-link-content .aui-dropdown2-section").eq(0).hide() };
	if (AJS.params.remoteUser === "guest") { AJS.$("#user-menu-link-content .aui-dropdown2-section").eq(1).hide() };
});