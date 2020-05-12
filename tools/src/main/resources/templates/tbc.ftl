<script>
  $(function () {
  $('[data-toggle="tooltip"]').tooltip()
})
</script>
<section class="post">
  <div class="container-fluid">
    <div class="card d mb-3" style="width: 100%;">
      <div class="card-header font-weight-bold">
        <i class="fas fa-calendar-day"></i>  Upcoming Events - Dates to be confirmed
      </div>
      <#list tbc_list as slot>
      <div class="card-body">
        <table style="width: 100%;">
          <tr>
            <td width="35%" class="font-weight-light" style="text-align:left"><a href="${slot.spotifyLink}" target="_blank"><img src="${slot.spotifyImgLink}" data-toggle="tooltip" data-placement="top" title="${slot.album} Spotify album link" alt="album" style="width:80px;height:80px;"></a><br><hr style="width:80px;margin-left:0;">TBC</td>
            <td width="50%" style="text-align:left;">
              <b>${slot.band}</b><br/>${slot.album}
            </td>
            <td width="15%"><a class="pure-button pure-button-active"
                               href="${slot.link}" target="_blank"><i
              class="fab fa-twitter-square"></i></a></td>
          </tr>
        </table>
        <hr/>
      </div>
     </#list>
      <hr/>
    </div>
  </div>
</section>
