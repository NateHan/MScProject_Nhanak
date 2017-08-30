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

function loadNAlert(url) {
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
});

$('#projectDataContent').on("click", '.tableProcessingToolSliders', function () {
    var targetArea = $(this).next('.tptSliderContent');
    var urlToPass = $(targetArea).attr('url');
    targetArea.load(urlToPass, function () {
            $(this).slideToggle()
        }
    );
});

/**
 *  Method which handles the submission of a manually-entered table row in the
 *  Project Data Workspace
 */
$('#projectDataContent').on('submit', '.manualRowAddForm', function (event) {
    // $(this) in this scenario is the form
    event.preventDefault();

    var data = [];
    //get all form text inputs and gather into array.
    $("form.manualRowAddForm input[type=text]").each(function () {
        var columnName = $(this).attr("name");
        var inputVal = $(this).val();
        data.push({colName: columnName, value: inputVal});
    });

    var tableName = $('.tableNameHiddenInput').val();

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    //is the ID of the container containing this form, which will need to display
    //a success or failure template based on the input.
    var targetAreaId = tableName + "manualRowAdd";

    var route = jsRoutes.controllers.ProjectsWorkSpaceController.manualAddNewRow(tableName);
    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {'X-CSRF-TOKEN': $('input[name=csrfToken]').attr('value')},
        success: function (data) {
            loadDoc('/projectworkspace/tool/response/Table%20Row/true', targetAreaId);
            var targetDivId = "tableRawData" + tableName; // from here below, reload table with added row
            document.getElementById(targetDivId).innerHTML = "";
            var url = "/projectworkspace/getOnlyTable/" + tableName
            loadDoc(url, targetDivId)
        },
        error: function (data) {
            loadDoc('/projectworkspace/tool/response/Table%20Row/false', targetAreaId)
        }
    })

});

/**
 * Method which will submit all inputs from the SQL View query form.
 */
$('#projectDataContent').on('submit', '.tableSQLViewQuery', function (event) {
    // $(this) in this scenario is the form
    event.preventDefault();
    var viewName = $(this).attr('viewName');
    var data = {
        viewName: viewName,
        query: $("#" + viewName + "TextAreaInput").val()
    };

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });
    var successDisplayArea = document.getElementById(viewName + "qryResultsDisplay");
    var route = jsRoutes.controllers.ProjectsWorkSpaceController.postQueryReturnResult();
    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {'X-CSRF-TOKEN': token},
        success: function (data) {
            successDisplayArea.innerHTML = data;
        },
        error: function (data) {
            var errMsgElemId = viewName + "ErrMsgTxt";
            document.getElementById(errMsgElemId).innerHTML = data.responseText
            successDisplayArea.innerHTML = "";
        }
    })

});


// Listens for a submission of #createProjectForm (in the dasboard)
// Linking with the jsRoutes method, it will POST the form and return the result in the targeted area.
$(document).on('submit', '#createProjectForm', function (event) {
    event.preventDefault();
    var data = {
        title: $('#projectTitleInputBox').val(),
        userName: $('#sessionUserName').val(),
        initialNote: $('#initNoteBox').val()
    };

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var route = jsRoutes.controllers.DashboardController.postNewProject();
    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify(data),
        contentType: 'application/json',
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

/* Listens for a submission of a #newNoteForm in the Project Workspace
If successful, the method inserts the notes into the Notes viewspace. If not, displays the error.
*/
$('#toolsrow').on('submit', '#newNoteForm', function (event) {

    event.preventDefault();

    var data = {
        projectTitle: $('#projectTitle').val(),
        noteTitle: $('#noteTitle').val(),
        noteAuthor: $('#noteAuthor').val(),
        noteContent: $('#noteContent').val()
    };


    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var route = jsRoutes.controllers.ProjectsWorkSpaceController.postNewNoteToDb();
    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {'X-CSRF-TOKEN': $('input[name=csrfToken]').attr('value')},
        success: function (data) {
            document.getElementById("newNoteUploadRow").innerHTML = data;
            $('#notesLoadZone').slideUp();
        },
        error: function (data) {
            console.log("Did not submit the note");
            document.getElementById("newNoteUploadRow").innerHTML = data;
        }

    });


});


//Listens for click on the icon in the data picker table. Appends the content to the Project
//Data viewspace.
$(document).on('click', '.dataPickerIconDiv', function () {
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

/** GOOGLE MAPS METHODS BELOW **/
/* Listens for the click of the Google Maps Button in the tableProcessingToolbar element
 * reveals the Google Maps div and then initializes/retrieves the map.
 * Grabs the map visualization div container and the actual generated HTML table
 * to pass onto the initMap() method.
 */
$(document).on('click', '.gmapInit', function () {
    var mapContainer = $(this).parents("div.tableProcessingToolBarContainer").prevAll("div.googleMapsContainer:first");
    var mapTarget = mapContainer.children(".gmap");
    var tableToBeMapped = mapContainer.siblings('.tableRawData')[0].getElementsByClassName('projDataTable')[0];
    console.log(tableToBeMapped);
    mapContainer.attr("style", ""); // makes element visible by removing "hidden" from attribute
    initMap(mapTarget, tableToBeMapped);
});

//Function initializes a Google Maps map within the table's processing area. The targetELem is
//a reserved Div for Google maps in the Project Data Workspace for each data table.
function initMap(targetElem, tableToMap) {

    var indexesOfLatAndLong = findIndexesOfLatAndLong(tableToMap);
    var allMappedPoints = getAllPointsAsObj(tableToMap, indexesOfLatAndLong);
    var averageLatLng = findMapCenter(allMappedPoints);
    var map = new google.maps.Map(targetElem[0], {
        zoom: 5,
        center: averageLatLng,
        mapTypeId: 'hybrid'
    });
    plotAllPoints(allMappedPoints, map);
}

/*
Method which takes all the points which will be calculated and plotted and finds the center.
This will be used to initialize the center of the map.
param: @allPoints is an Array of JSON in this format: {"animalId": _, "date": _, "lat": _, "long": _}
return: returns a single JSON object in the format of {lat:_, lng:_} which is the calculated center
of the map.
 */
function findMapCenter(allPoints) {
    var averageLat = 0;
    var averageLong = 0;
    $.each(allPoints, function (count, pointObj) {
        averageLat = +((averageLat + pointObj.lat).toFixed(6)); // '+' transforms it back to a number
        averageLong = +((averageLong + pointObj.long).toFixed(6));
    });
    return {
        "lat": +((averageLat / allPoints.length).toFixed(6)),
        "lng": +((averageLong / allPoints.length).toFixed(6))
    };
}

/*
 Identifies the index of the column of the table which represents the latitude and longitude
 @param: tableElem a full <table></table>
 @return: a JSON object in the format {latIndex: _, lngIndex: _};
 */
function findIndexesOfLatAndLong(tableElem) {
    var tableLatIndex = $(tableElem).find('th:contains("latitude"), th:contains("Latitude"), th:contains("LATITUDE")')
        .first().index();
    var tableLongIndex = $(tableElem).find('th:contains("longitude"), th:contains("Longitude"), th:contains("LONGITUDE")')
        .first().index();
    return {latIndex: tableLatIndex, lngIndex: tableLongIndex};
}

/*
Method which takes an entire <table></table> elem and the indexes which identify its latitude
and longitude and returns an array of all the geolocational points stored in the form of
a JSON array: [ { "animalId": "_", "date":"_",  "lat":_num_, "long":_num_}, ...etc]
 */
function getAllPointsAsObj(tableData, indexesOfLatAndLong) {
    var allRows = $(tableData).find("tr.projDataContentRow");
    var animalIdIndex = $(tableData).find('th:contains("id"), th:contains("ID"), th:contains("Id")')
        .first().index();
    var dateIndex = $(tableData).find('th:contains("date"), th:contains("Date"), th:contains("DATE")')
        .first().index();
    var allPoints = [];
    $.each(allRows, function (index, row) {
        var id = $(row).find('td:eq(' + animalIdIndex + ')').html();
        var date = $(row).find('td:eq(' + dateIndex + ')').html();
        var lat = Number($(row).find('td:eq(' + indexesOfLatAndLong.latIndex + ')').html());
        var long = Number($(row).find('td:eq(' + indexesOfLatAndLong.lngIndex + ')').html());
        var point = {
            "animalId": id,
            "date": date,
            "lat": lat,
            "long": long
        };
        allPoints.push(point);
    });
    return allPoints;
};

/*
Takes all the points from the referenced data table and displays them on the map.
@param allPoints - a JSON array in the form of:
      [ { "animalId": "_", "date":"_",  "lat":_num_, "long":_num_}
@param map - the current map displayed and on which we would like to display the points
 */
function plotAllPoints(allPoints, map) {
    var idsToColours = generateColorForUniqueId(allPoints); // Array [ {animalId:_, color:_}, {...} ]
    var circlePath = google.maps.SymbolPath.CIRCLE;
    $.each(allPoints, function(index, point){
        var latLng = new google.maps.LatLng(point.lat, point.long);
        var colour = null // use point.AnimalId as key and use it to retrieve the color value from idsToColours
        var marker = new google.maps.Marker({
            position: latLng,
            fillColor: colour,
            fillOpacity: 0.8,
            icon: {
                path: circlePath,
                scale: 3
            },
            map: map
            // do stroke colour too if it acts weird
        });
    });
}

/**
 * creates a JSON object of unique animalId's to a unique colour.
 * @param allPoints all the points in the
 * @return a JSON array of [ {animalId:"_", colour:"_"}]
 */
function generateColorForUniqueId(allPoints) {
    var uniqueIds = getAllUniqueIds(allPoints);
    var usedColours = [];
    var idsToColours = [];
    for(var i = 0; i < uniqueIds.length; i++) {
        // START HERE
        //
        // retrieve a color
        // if it's in usedColours, retrieve another color
        // if it's not: push an obj in idsToColours, and push just colour to used Colours,
    }
    return idsToColours;
}

/**
 * gets all unique ID's for the list of all the points.
 * @param allPoints a JSON array in the form of:
 [ { "animalId": "_", "date":"_",  "lat":_num_, "long":_num_}, etc... ]
 */
function getAllUniqueIds(allPoints) {
    var idsMapped = [];
    for (var i = 0; i < allPoints.length; i++) {
        var idIsMapped = false;
        idsMapped.forEach(function(id) {
            if (allPoints[i].animalId === id) {
                idIsMapped = true
            };
        });
        if (idIsMapped === false) {
            idsMapped.push(allPoints[i].animalId);
        }
    }
    return idsMapped;
}

/** GOOGLE MAPS' METHODS ABOVE **/