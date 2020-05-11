package com.timstwitterlisteningparty.tools.parser

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Component
class TimeSlotFileCreator : HtmlFileCreator {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun createFiles(fileName: String, inputStream: InputStream?, writeToFile: Boolean): Map<String, String> {

    val beans = TimeSlotReader(fileName, inputStream).timeSlots
    beans.forEach { logger.debug("Read in Bean {}", it) }
    val tbd = beans.stream()
      .filter { it.isoDate.year == 1970 }.collect(Collectors.toList())
    val completed = beans.stream()
      .filter { it.isoDate.year != 1970 && it.isoDate.toLocalDate().isBefore(LocalDate.now()) }
      .collect(Collectors.toList())
    val upcoming = beans.stream()
      .filter { it.isoDate.year != 1970 && it.isoDate.toLocalDate().isBefore(LocalDate.now()).not() }
      .collect(Collectors.toList())
    tbd.forEach { logger.debug("Dates to be confirmed {}", it) }
    completed.forEach { logger.debug("Completed listening {}", it) }
    upcoming.forEach { logger.debug("Upcoming listening {}", it) }
    // the new card based table
    val upcomingHtmlCard = buildTableCard(upcoming)
    val upcomingFileCard = File("snippets/upcoming-time-slots-card.html")
    val dateTbdHtml = buildTbcCards(tbd)
    val dateTbdFile = File("snippets/date-tbd-time-slots.html")
    val completedHtml = buildCompletedTableCards(completed)
    val completedFile = File("snippets/completed-time-slots.html")
    val allOneTableHtml = buildAllTable(beans)
    val allOneTableFile = File("snippets/all-time-slots.html")
    // if called from Lambda we can't write to the file
    if (writeToFile) {
      completedFile.writeText(completedHtml)
      dateTbdFile.writeText(dateTbdHtml)
      allOneTableFile.writeText(allOneTableHtml)
      upcomingFileCard.writeText(upcomingHtmlCard)
    }
    logger.debug("Upcoming\n {} \nDateTbd \n{} \ncompleted\n {} \nAll \n{}", upcomingHtmlCard, dateTbdHtml, completedHtml, allOneTableHtml)

    return mapOf(
      Pair("snippets/${dateTbdFile.name}", dateTbdHtml),
      Pair("snippets/${completedFile.name}", completedHtml),
      Pair("snippets/${allOneTableFile.name}", allOneTableHtml),
      Pair("snippets/${upcomingFileCard.name}", upcomingHtmlCard))
  }


  private fun buildAllTable(beans: List<TimeSlot>): String {
    val template = FreeMarkerUtils().getFreeMarker(ALL_FTL)
    val input: Map<String, List<TimeSlot>> = mapOf(Pair("all_list", beans.sortedBy { it.album }))
    val htmlStr = StringWriter()
    template.process(input, htmlStr)
    return htmlStr.toString()
  }


  /**
   * Uses template tbc.ftl to create the tbc card
   */
  private fun buildTbcCards(tbd: List<TimeSlot>): String {
    val template = FreeMarkerUtils().getFreeMarker(TBC_FTL)
    val input: Map<String, List<TimeSlot>> = mapOf(Pair("tbc_list", tbd.sortedBy { it.band }))
    val htmlStr = StringWriter()
    template.process(input, htmlStr)
    return htmlStr.toString()
  }

  private fun buildCompletedTableCards(completed: List<TimeSlot>): String {
    val template = FreeMarkerUtils().getFreeMarker(ARCHIVE_FTL)
    val input: Map<String, List<TimeSlot>> = mapOf(Pair("completed_list", completed.sortedByDescending{ it.isoDate }))
    val htmlStr = StringWriter()
    template.process(input, htmlStr)
    return htmlStr.toString()
  }


  private fun buildTableCard(slots: List<TimeSlot>): String {
    val sortedSlots = slots.sortedBy { it.isoDate }
    var section = "<section class=\"post\">\n<div class=\"container-fluid\">"

    var hr = ""
    var date = sortedSlots.first().isoDate
    section = section.plus("      <div class=\"card d mb-3 border-dark\" style=\"width: 100%;\">\n" +
      "        <div class=\"card-header font-weight-bold\">\n" +
      "          <i class=\"fas fa-calendar-day\"></i> ${date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))} \n" +
      "        </div>")


    sortedSlots.forEach {

      // new card header required if we have moved on
      if (it.isoDate.toLocalDate().isAfter(date.toLocalDate())) {
        section = section.plus("      </div><div class=\"card d mb-3\" style=\"width: 100%;\">\n" +
          "        <div class=\"card-header font-weight-bold\">\n" +
          "          <i class=\"fas fa-calendar-day\"></i> ${it.isoDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))} \n" +
          "        </div>")
      } else {
        section = section.plus(hr)
        hr = "<hr/>"
      }
      // build the card body
      section = section.plus(it.buildHtmlCardBody())
      date = it.isoDate
    }

    return section.plus("\n</div></div>\n</section>")
  }


}
