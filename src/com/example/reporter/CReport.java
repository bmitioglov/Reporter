package com.example.reporter;



public class CReport
{
	public enum CalendarType 
	{
		Year,
		Month,
		Day
	};
	public long m_reportID=(long) -1;
	public String m_reportName="";
	public CalendarType m_calendarType=CalendarType.Day;
};
