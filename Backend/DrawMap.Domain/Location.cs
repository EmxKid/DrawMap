namespace DrawMap.Domain;

public class Location
{
    public string? Id { get; set; }
    public double Longitude { get; set; }
    public double Latitude { get; set; }
    public string RouteId { get; set; }
    public DateTime Timestamp { get; set; }
    public Photo? Photo { get; set; }
}