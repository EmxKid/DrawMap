namespace DrawMap.Domain;

public class Route
{
    public string? Id { get; set; }
    public List<Photo>? Photos { get; set; }
    public Location[]? Locations { get; set; }
}