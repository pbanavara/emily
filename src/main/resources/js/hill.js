$(function() {
    getData("");
    function getData(supervisorId) {
        $.get("/trainingBySupervisor?supervisorTransactionID=" + supervisorId, function(data) {
            var id = $("#subContents").attr("id");
            var cboxId = $("#cbox").attr("id");
            for (var i = 0;i<data.length; ++i) {
            console.log("Get request Output");
            console.log(data[i]);
                var incomingMeetingEmail = data[i]["incomingMeetingEmail"];
                var associatedMeetingRequest = data[i]["associatedMeetingRequest"];
                var newDiv = $("#subContents").clone();
                var newId = id + i;
                var newCboxId = cboxId + i;
                newDiv.attr("id", newId);
                newDiv.find(".from").val(incomingMeetingEmail["emailFrom"]);
                newDiv.find(".to").val(incomingMeetingEmail["emailTo"]);
                newDiv.find(".subject").val(incomingMeetingEmail["emailSubject"]);
                var overrideText = data[i]["supervisorOverrideEmailText"];
                var debugText = data[i]["debugAndExceptionMessages"]
                if(overrideText == "") {
                    newDiv.find(".emailText").val(incomingMeetingEmail["emailText"]);
                } else {
                    newDiv.find(".emailText").val(overrideText);
                }
                if (debugText != "") {
                    newDiv.find(".exceptionText").val(debugText);
                }

                newDiv.find(".mrId").val(associatedMeetingRequest["ID"]);
                newDiv.find(".recheckButton").attr("id", newCboxId);
                $("div").data(newId, data[i]);
                $(".emailContents").append(newDiv);
                $(".emailContents").append("<br>");
                var listOfOutputEmails = data[i]["outgoingMeetingEmails"]
                for (var j=0;j<listOfOutputEmails.length;++j) {
                    var oEmail = $("#outputMeeting").clone();
                    var opId = newId + j;
                    oEmail.attr("id", opId);
                    var outgoingEmail = listOfOutputEmails[j];
                    oEmail.find(".opFrom").val(outgoingEmail["from"]);
                    oEmail.find(".opTo").val(outgoingEmail["to"]);
                    oEmail.find(".opSubject").val(outgoingEmail["subject"]);
                    oEmail.find(".opText").val(outgoingEmail["text"]);
                    newDiv.find('.childRow').append(oEmail)
                newDiv.find("#outputMeeting").hide();
                }
            }
            $("#subContents").hide();
        });
    }

    $(document).on('click', '.recheckButton', function() {
            var id = $(this).parents('.subContents').attr('id');
            var removeId = "#" + id;
            var postData = $("div").data(id);
            var supervisorId = postData["supervisorTransactionID"];
            var newVal = $("#" + id).find(".emailText").val();
            postData.supervisorOverrideEmailText = newVal;
            postData.supervisorVerified = "false";
            console.log(postData);
            $.ajax({
                type: 'POST',
                url: '/trainingBySupervisor',
                data: JSON.stringify(postData),
                success: function(data) {
                   console.log("Output from post for Recheck");
                   console.log(data);
                   $("#subContents").show();
                   $(removeId).remove();
                   getData(supervisorId);
                },
                contentType: "application/json",
                dataType: 'json'
            });
    });

    $(document).on('click', '.verifiedButton', function() {
            var id = $(this).parents('.subContents').attr('id');
            var removeId = "#" + id;
            var postData = $("div").data(id);
            var val = $("#" + id).find(".emailText").val();
            if (postData["supervisorOverrideEmailText"] == "") {
              postData.supervisorOverrideEmailText = val;
            }
            postData.supervisorVerified = "true";
            console.log(postData);
            $.ajax({
                type: 'POST',
                url: '/trainingBySupervisor',
                data: JSON.stringify(postData),
                contentType: "application/json",
                dataType: 'json',
                success: function(data) {
                  $("#subContents").show();
                  $(removeId).remove();
                  console.log("output from the post for verify");
                  console.log(data);
                  getData("");
                }
            });
    });
});