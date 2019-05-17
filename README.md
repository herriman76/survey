# Survey biz component
background survey core code via urocissa rpc service which was finished by liujun all.

server layer:accept survey task from outside and send subtask to middle layer.
middle layer:accept sub task and choose a client, then send it to the client who will do this job.
              job include online and offline subtask(survey by person from telepnone survey center).
client layer:accept sub task and finish it,then send the status of it to middle.

all the comunication between them are throuth RPC tool- urocissa which finished by my colleage and me.

【remark】
The function of survey is show in survey_profile.png & survey_detail.png
