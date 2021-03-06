package cron

import cats.data.{Kleisli, NonEmptyChain, ValidatedNec}
import cron.parser.{DayOfMonth, Hour, Minute}

package object parser {
  /* domain entities */
  trait Buildable[T] {
    def apply(list: List[Int]): T
  }

  type ValidatedTo[A] = Result[FieldType,A]

  case class LineTokens(
                         maybeMinute: String,
                         maybeHour: String,
                         maybeDayOfMonth: String,
                         maybeMonth: String,
                         maybeDayOfWeek: String,
                         maybeCommand: String
                       )


  sealed trait FieldType
  final case class Entry(value: Int) extends FieldType {
    override def toString: String = value.toString
  }
  final case class Asterisk(maybeStep: Option[Int] = None) extends FieldType
  final case class Range(start: Int, end: Int, maybeStep: Option[Int] = None) extends FieldType {
    override def toString: String = s"$start-$end" + maybeStep.map(v => s"/$v").getOrElse("")
  }
  final case class ListOfEntries(list: List[Entry]) extends FieldType {
    override def toString: String = list.mkString(",")
  }
  final case class ListOfRanges(list: List[Range]) extends FieldType
  final case class LiteralDay(value: String) extends FieldType {
    override def toString: String = value
  }
  final case class LiteralMonth(value: String) extends FieldType {
    override def toString: String = value
  }

  /* Field hierarchy */
  sealed trait Field extends Any

  final case class Minute(listOfMinutes: List[Int]) extends Field {
    override def toString: String = listOfMinutes.mkString(" ")
  }
  object Minute {
    implicit val minuteBuildable: Buildable[Minute] = (list: List[Int]) => Minute(list)
  }
  final case class Hour(listOfHours: List[Int]) extends Field {
    override def toString: String = listOfHours.mkString(" ")
  }
  object Hour {
    implicit val hourBuildable: Buildable[Hour] = (list: List[Int]) => Hour(list)
  }
  final case class DayOfMonth(listOfDays: List[Int]) extends Field {
    override def toString: String = listOfDays.mkString(" ")
  }
  object DayOfMonth {
    implicit val dayOfMonthBuildable: Buildable[DayOfMonth] = (list: List[Int]) => DayOfMonth(list)
  }
  final case class Month(listOfMonths: List[Int]) extends Field {
    override def toString: String = listOfMonths.mkString(" ")
  }
  object Month {
    implicit val monthBuildable: Buildable[Month] = (list: List[Int]) => Month(list)
  }
  final case class DayOfWeek(listOfDays: List[Int]) extends Field {
    override def toString: String = listOfDays.mkString(" ")
  }
  object DayOfWeek {
    implicit val dayOfWeekBuildable: Buildable[DayOfWeek] = (list: List[Int]) => DayOfWeek(list)
  }
  final case class Command(value: String) extends AnyVal with Field {
    override def toString: String = value
  }

  final case class CronLine(
                           minute: Minute,
                           hour: Hour,
                           dayOfMonth: DayOfMonth,
                           month: Month,
                           dayOfWeek: DayOfWeek,
                           command: Command
                           )

  /* Error hierarchy */
  sealed trait Error {
    val message: String
    override def toString: String = message
  }

  final case class ParsingError(message: String) extends Error
  final case class InvalidFormat(message: String) extends Error
  final case class IllegalValue(message: String) extends Error

  /* functional type aliases */
  type ValidationResult[A] = ValidatedNec[Error, A]
  type Result[A,B] = Kleisli[Either[Error,*],A,B]
  type ErrorOr[A] = Either[NonEmptyChain[Error],A]
}
