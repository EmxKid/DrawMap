namespace DrawMap.Domain;

public class Location
{
    public string? Id { get; set; }
    public double Longitude { get; set; }
    public double Latitude { get; set; }
    public DateTime Timestamp { get; set; }
    public int VisitFrequency { get; set; }
}