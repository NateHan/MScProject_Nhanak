// dynamically loads a view by the URL of the router route to load and
// the id of the element to load the page into
function loadDoc(myUrl, id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById(id).innerHTML =
                this.responseText;
        }
    };
    xhttp.open("GET", myUrl, true);
    xhttp.send();
}

function loadNAlert(url ) {
    window.alert("This thing loaded and passed me this: " + url)
}

// On the project sliders click, it loads the URL of the controller method route, and then
// activates a dropdown menu, which animates once the information is loaded
$(document).ready(function () {

    $('.appSliderContent').hide();

    $('.appSliders').on('click', function () {
        var targetArea = $(this).next('.appSliderContent');
        var urlToPass = $(targetArea).attr('url');
        targetArea.load(urlToPass, function () {
                $(this).slideToggle()
            }
        );
    });
})

$('#projectDataContent').on("click", '.tableProcessingToolSliders', function() {
        var targetArea = $(this).next('.tptSliderContent');
        targetArea.slideToggle(); // remove this from here when it loads a URL later, it is below
        /* reactivate later when these appsliders have URLs
        var urlToPass = $(targetArea).attr('url');
        targetArea.load(urlToPass, function () {
                $(this).slideToggle()
            }
        );
         */
});



// Listens for a submission of #createProjectForm (in the dasboard)
// Linking with the jsRoutes method, it will POST the form and return the result in the targeted area.
$(document).on('submit', '#createProjectForm', function (event) {

    event.preventDefault();
    var data = {
        title: $('#projectTitleInputBox').val(),
        userName: $('#sessionUserName').val(),
        initialNote: $('#initNoteBox').val()
    }

    var token =  $('input[name="csrfToken"]').attr('value')
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var route = jsRoutes.controllers.DashboardController.postNewProject()
    $.ajax({
        url: route.url,
        type: route.type,
        data : JSON.stringify(data),
        contentType : 'application/json',
        headers: {'X-CSRF-TOKEN': $('input[name=csrfToken]').attr('value')},
        success: function (data) {
            var projName = JSON.stringify(data.newProjectName).replace(/^"(.*)"$/, '$1');
            var successURL = "/dashboard/projects/sliderSubmitResponse/newProjSuccess/" + projName;
            loadDoc(successURL, 'createNewProjectDiv')
        },
        error: function (data) {
            loadDoc('/dashboard/projects/sliderSubmitResponse/newProjFail/NoProjectCreated', 'createNewProjectDiv')
        }
    })

});

//Listens for click on the icon in the data picker table. Appends the content to the Project
//Data viewspace.
$(document).on('click', '.dataPickerIconDiv', function() {
    var targetArea = $(this).children("i");
    var urlToPass = $(targetArea).attr('url');
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            $("#projectDataContent").prepend(this.responseText);
            $('.tptSliderContent').hide(); // hides the slider content of the table processing tools
        }
    };
    xhttp.open("GET", urlToPass, true);
    xhttp.send();

});
