using DrawMap.DataSources;
using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;

namespace DrawMap.Repositories;

public class RouteRepository : IRouteRepository
{
    private readonly DrawMapDbContext _context;

    public RouteRepository(DrawMapDbContext context)
    {
        _context = context;
    }

    public async Task<string> AddRoute(Route route, CancellationToken cancellationToken)
    {
        _context.Routes.Add(route);
        await _context.SaveChangesAsync(cancellationToken);
        return route.Id ?? string.Empty;
    }

    public async Task<Route?> GetRoute(string routeId, CancellationToken cancellationToken)
    {
        return await _context.Routes
            .Include(r => r.Locations)
            .FirstOrDefaultAsync(r => r.Id == routeId, cancellationToken: cancellationToken);
    }

    public async Task<List<Route>> GetRoutes(CancellationToken cancellationToken)
    {
        return await _context.Routes
            .Include(r => r.Locations)
            .ToListAsync(cancellationToken);
    }

    public async Task<Route?> UpdateRoute(string routeId, Route route, CancellationToken cancellationToken)
    {
        var existing = await _context.Routes
            .Include(r => r.Locations)
            .FirstOrDefaultAsync(r => r.Id == routeId, cancellationToken: cancellationToken);

        if (existing is null)
            return null;

        existing.TotalDistance = route.TotalDistance;
        existing.StartTime = route.StartTime;
        existing.EndTime = route.EndTime;
        existing.Locations = route.Locations;

        await _context.SaveChangesAsync(cancellationToken);
        return existing;
    }

    public async Task<bool> DeleteRoute(string routeId, CancellationToken cancellationToken)
    {
        var existing = await _context.Routes.FindAsync(routeId);
        if (existing is null)
            return false;

        _context.Routes.Remove(existing);
        await _context.SaveChangesAsync(cancellationToken);
        return true;
    }
}
