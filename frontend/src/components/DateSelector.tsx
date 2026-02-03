interface DateSelectorProps {
  selectedDate: string
  onChange: (date: string) => void
}

function isWeekend(dateString: string): boolean {
  const date = new Date(dateString)
  const day = date.getDay()
  return day === 0 || day === 6 // Dimanche = 0, Samedi = 6
}

function getNextWeekday(dateString: string): string {
  const date = new Date(dateString)
  const day = date.getDay()
  if (day === 0) date.setDate(date.getDate() + 1) // Dimanche → Lundi
  if (day === 6) date.setDate(date.getDate() + 2) // Samedi → Lundi
  return date.toISOString().split('T')[0]
}

function DateSelector({ selectedDate, onChange }: DateSelectorProps) {
  const today = new Date().toISOString().split('T')[0]

  const handleChange = (value: string) => {
    if (isWeekend(value)) {
      onChange(getNextWeekday(value))
    } else {
      onChange(value)
    }
  }

  return (
    <div className="date-selector">
      <label htmlFor="view-date">Date :</label>
      <input
        type="date"
        id="view-date"
        value={selectedDate}
        onChange={(e) => handleChange(e.target.value)}
        min={today}
      />
      <span className="weekend-note">(Lun-Ven uniquement)</span>
    </div>
  )
}

export default DateSelector
