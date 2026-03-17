namespace DrawMap.Domain;

public class Route
{
    public string? Id { get; set; }
    public double TotalDistance { get; set; }
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    public List<Location>? Locations { get; set; }
}